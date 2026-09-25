package me.thegiggitybyte.chathistory;

import eu.pb4.placeholders.api.parsers.NodeParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
//? if >= 26.1 {
/*import eu.pb4.placeholders.api.ServerPlaceholderContext;
*///?} else {
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.TextParserV1;
//?}

public final class SeparatorFormatter {
    private SeparatorFormatter() {
    }

    public static Component format(String input, ServerPlayer player) {
        //? if >= 26.1 {
        /*var parser = NodeParser.builder()
                .serverPlaceholders()
                .quickText()
                .simplifiedTextFormat()
                .build();
        return parser.parseComponent(input, ServerPlaceholderContext.of(player).asParserContext());
        *///?} else {
        return NodeParser.merge(Placeholders.DEFAULT_PLACEHOLDER_PARSER, new TextParserV1())
                .parseText(input, PlaceholderContext.of(player).asParserContext());
        //?}
    }
}
