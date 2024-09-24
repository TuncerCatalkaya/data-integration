package org.datamigration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum DelimiterModel {
    COMMA(","),
    SEMICOLON(";"),
    TAB("\\t"),
    PIPE("|"),
    SPACE(" ");

    private final String delimiterCharacter;

    private static final Map<String, DelimiterModel> CHARACTER_TO_ENUM = new HashMap<>();
    private static final Map<DelimiterModel, String> ENUM_TO_CHARACTER = new EnumMap<>(DelimiterModel.class);

    static {
        for (DelimiterModel delimiter : values()) {
            CHARACTER_TO_ENUM.put(delimiter.getDelimiterCharacter(), delimiter);
            ENUM_TO_CHARACTER.put(delimiter, delimiter.getDelimiterCharacter());
        }
    }

    public static DelimiterModel fromCharacter(String character) {
        return CHARACTER_TO_ENUM.get(character);
    }

    public static String toCharacter(DelimiterModel delimiter) {
        return ENUM_TO_CHARACTER.get(delimiter);
    }
}
