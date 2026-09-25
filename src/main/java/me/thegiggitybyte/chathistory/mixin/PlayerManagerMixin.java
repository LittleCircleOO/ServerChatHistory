package me.thegiggitybyte.chathistory.mixin;

import me.thegiggitybyte.chathistory.ChatHistory;
import me.thegiggitybyte.chathistory.compat.StyledChatCompat;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
//? >= 1.20.2 {
import net.minecraft.server.network.CommonListenerCookie;
//?}
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Z)V",
            at = @At("HEAD"))
    public void cacheGameMessage(Component message, Function<ServerPlayer, Component> playerMessageFactory, boolean overlay, CallbackInfo ci) {
        if (overlay) return;
        ChatHistory.cache(message);
    }

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At("HEAD"))
    public void cachePlayerMessage(PlayerChatMessage message, Predicate<ServerPlayer> filterPredicate, @Nullable ServerPlayer sender, ChatType.Bound params, CallbackInfo ci) {
        Component styled = StyledChatCompat.getFormatted(message);
        ChatHistory.cache(styled != null ? styled : params.decorate(message.decoratedContent()));
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void sendCachedMessages(Connection connection, ServerPlayer player
            //? >= 1.20.2 {
            , CommonListenerCookie clientData
            //?}
            , CallbackInfo ci) {
        if (!ChatHistory.canView(player)) {
            return;
        }

        ChatHistory.sendSeparator(player, ChatHistory.HISTORY_START);
        for (var message : ChatHistory.MESSAGE_CACHE) {
            message.send(player);
        }
        ChatHistory.sendSeparator(player, ChatHistory.HISTORY_END);
    }
}
