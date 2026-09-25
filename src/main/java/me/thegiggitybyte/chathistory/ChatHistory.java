package me.thegiggitybyte.chathistory;

import com.google.common.collect.EvictingQueue;
import me.thegiggitybyte.chathistory.message.CachedMessage;
import net.darktree.simpleconfig.SimpleConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.util.Queue;

@Environment(EnvType.SERVER)
public class ChatHistory implements DedicatedServerModInitializer {
    public static Queue<CachedMessage> MESSAGE_CACHE;

    @Override
    public void onInitializeServer() {
        var config = SimpleConfig
                .of("chathistory")
                .provider(fileName -> "# number of messages to store\ncache-size=25\n# persist history across restarts (true/false)\npersistence=true")
                .request();

        int cacheSize = config.getOrDefault("cache-size", 25);
        MESSAGE_CACHE = EvictingQueue.create(cacheSize);
        ChatHistoryStore.init(Boolean.parseBoolean(config.getOrDefault("persistence", "true")), cacheSize, MESSAGE_CACHE);
    }

    public static void cache(Component message) {
        MESSAGE_CACHE.add(new CachedMessage(message));
        ChatHistoryStore.append(message);
    }
}
