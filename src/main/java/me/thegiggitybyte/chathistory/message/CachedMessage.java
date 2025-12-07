package me.thegiggitybyte.chathistory.message;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CachedMessage {
    private final Component message;

    public CachedMessage(Component message) {
        this.message = message;
    }

    public void send(ServerPlayer player) {
        // 'false' meant system message in older versions; sendSystemMessage implies this now.
        player.sendSystemMessage(message);
    }
}