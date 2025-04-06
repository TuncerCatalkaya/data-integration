package org.dataintegration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum DelimiterModel {
    COMMA(','),
    SEMICOLON(';'),
    TAB('\t'),
    PIPE('|'),
    SPACE(' ');

    private static final Map<Character, DelimiterModel> CHARACTER_TO_ENUM = new HashMap<>();
    private static final Map<DelimiterModel, Character> ENUM_TO_CHARACTER = new EnumMap<>(DelimiterModel.class);

    private final char delimiterCharacter;

    static {
        for (DelimiterModel delimiter : values()) {
            CHARACTER_TO_ENUM.put(delimiter.getDelimiterCharacter(), delimiter);
            ENUM_TO_CHARACTER.put(delimiter, delimiter.getDelimiterCharacter());
        }
    }

    public static DelimiterModel fromCharacter(char character) {
        return CHARACTER_TO_ENUM.get(character);
    }

    public static char toCharacter(DelimiterModel delimiter) {
        return ENUM_TO_CHARACTER.get(delimiter);
    }
}
