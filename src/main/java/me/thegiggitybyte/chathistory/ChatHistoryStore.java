package me.thegiggitybyte.chathistory;

import me.thegiggitybyte.chathistory.message.CachedMessage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
//? if >=1.21.1 {
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.ComponentSerialization;
//?}

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Queue;

public final class ChatHistoryStore {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("chathistory-history.jsonl");
    private static final int COMPACT_FACTOR = 2;

    private static Queue<CachedMessage> cache;
    private static BufferedWriter writer;
    private static boolean enabled;
    private static int capacity;
    private static int linesOnDisk;

    private ChatHistoryStore() {
    }

    public static void init(boolean persistenceEnabled, int cacheCapacity, Queue<CachedMessage> messageCache) {
        enabled = persistenceEnabled;
        capacity = cacheCapacity;
        cache = messageCache;
        if (!enabled) {
            return;
        }

        linesOnDisk = load();
        Runtime.getRuntime().addShutdownHook(new Thread(ChatHistoryStore::shutdown));
    }

    public static void append(Component message) {
        if (!enabled) {
            return;
        }

        try {
            if (writer == null) {
                writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

            String encoded = encode(message);
            if (encoded == null) {
                return;
            }

            writer.write(encoded);
            writer.newLine();
            writer.flush();
            linesOnDisk++;
        } catch (IOException ignored) {
        }
    }

    private static int load() {
        int lines = 0;

        try (BufferedReader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines++;
                if (line.isEmpty()) {
                    continue;
                }

                Component message = decode(line);
                if (message != null) {
                    cache.add(new CachedMessage(message));
                }
            }
        } catch (IOException ignored) {
            return 0;
        }

        return lines;
    }

    private static void shutdown() {
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
                writer = null;
            }

            if (linesOnDisk > capacity * COMPACT_FACTOR) {
                compact();
            }
        } catch (IOException ignored) {
        }
    }

    private static void compact() throws IOException {
        Path tmp = FILE.resolveSibling(FILE.getFileName() + ".tmp");

        try (BufferedWriter rewrite = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            for (CachedMessage message : cache) {
                String encoded = encode(message.component());
                if (encoded != null) {
                    rewrite.write(encoded);
                    rewrite.newLine();
                }
            }
        }

        Files.move(tmp, FILE, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String encode(Component message) {
        //? if >=1.21.1 {
        return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, message)
                .result()
                .map(Object::toString)
                .orElse(null);
        //?} else {
        /*return Component.Serializer.toJson(message);
        *///?}
    }

    private static Component decode(String line) {
        //? if >=1.21.1 {
        return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(line))
                .result()
                .orElse(null);
        //?} else {
        /*return Component.Serializer.fromJson(line);
        *///?}
    }
}
