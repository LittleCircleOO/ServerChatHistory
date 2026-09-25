package me.thegiggitybyte.chathistory.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;

public final class StyledChatCompat {
    private static final boolean ENABLED = FabricLoader.getInstance().isModLoaded("styledchat");

    private static final String[] DUCK_INTERFACES = {
            "eu.pb4.styledchat.ducks.ExtSignedMessage",
            "eu.pb4.styledchat.ducks.ExtPlayerChatMessage"
    };

    private StyledChatCompat() {
    }

    public static Component getFormatted(Object message) {
        if (!ENABLED) {
            return null;
        }

        for (String name : DUCK_INTERFACES) {
            try {
                Class<?> iface = Class.forName(name);
                if (!iface.isInstance(message)) {
                    continue;
                }

                Method getter = iface.getMethod("styledChat_getArg", String.class);
                Object value = getter.invoke(message, "override");
                if (value instanceof Component component && !component.getString().isEmpty()) {
                    return component;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
            }
        }

        return null;
    }
}
