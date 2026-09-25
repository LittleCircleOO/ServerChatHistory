package me.thegiggitybyte.chathistory;

import com.google.common.collect.EvictingQueue;
import me.thegiggitybyte.chathistory.message.CachedMessage;
import net.darktree.simpleconfig.SimpleConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
//? if >= 1.21.11 {
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
//?}

import java.util.Queue;

@Environment(EnvType.SERVER)
public class ChatHistory implements DedicatedServerModInitializer {
    public static Queue<CachedMessage> MESSAGE_CACHE;
    public static int PERMISSION_LEVEL;
    public static String HISTORY_START;
    public static String HISTORY_END;

    @Override
    public void onInitializeServer() {
        var config = SimpleConfig
                .of("chathistory")
                .provider(fileName -> """
                        # Number of chat messages kept in memory (and on disk when persistence is enabled). \
                        Once full, the oldest message is dropped.
                        cache-size=25

                        # Save cached history to disk and restore it on restart (true/false).
                        persistence=true

                        # Minimum permission level required to receive the history on join.
                        #   0 = every player (default)
                        #   1 = moderators   (vanilla OP level 1)
                        #   2 = gamemasters  (vanilla OP level 2)
                        #   3 = admins       (vanilla OP level 3)
                        #   4 = owners       (vanilla OP level 4)
                        # Vanilla OPs usually hold level 4; values above 4 are treated as 4.
                        permission-level=0

                        # Separator message sent BEFORE the replayed history, supports TextPlaceholderAPI
                        # formatting tags (e.g. <gray><italic>...</italic></gray>) and placeholders
                        # (e.g. %player:name%, %server:online%). Leave empty to send nothing.
                        # Separator messages are NOT counted towards cache-size.
                        history-start=

                        # Separator message sent AFTER the replayed history, same syntax as history-start.
                        # Leave empty to send nothing. Not counted towards cache-size.
                        history-end=""")
                .request();

        int cacheSize = config.getOrDefault("cache-size", 25);
        MESSAGE_CACHE = EvictingQueue.create(cacheSize);
        PERMISSION_LEVEL = config.getOrDefault("permission-level", 0);
        HISTORY_START = config.getOrDefault("history-start", "");
        HISTORY_END = config.getOrDefault("history-end", "");
        ChatHistoryStore.init(Boolean.parseBoolean(config.getOrDefault("persistence", "true")), cacheSize, MESSAGE_CACHE);
    }

    public static void cache(Component message) {
        MESSAGE_CACHE.add(new CachedMessage(message));
        ChatHistoryStore.append(message);
    }

    public static boolean canView(ServerPlayer player) {
        if (PERMISSION_LEVEL <= 0) {
            return true;
        }

        int level = Math.min(4, Math.max(1, PERMISSION_LEVEL));

        //? if >= 1.21.11 {
        var permissionLevel = switch (level) {
            case 1 -> PermissionLevel.MODERATORS;
            case 2 -> PermissionLevel.GAMEMASTERS;
            case 3 -> PermissionLevel.ADMINS;
            default -> PermissionLevel.OWNERS;
        };
        return player.permissions().hasPermission(new Permission.HasCommandLevel(permissionLevel));
        //?} else {
        /*return player.hasPermissions(level);
        *///?}
    }

    public static void sendSeparator(ServerPlayer player, String raw) {
        if (raw == null || raw.isEmpty()) {
            return;
        }

        player.sendSystemMessage(SeparatorFormatter.format(raw, player));
    }
}
