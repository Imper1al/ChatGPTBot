package org.chatgpt.utils;

public class StringUtils {

    public StringUtils() {

    }

    public static String addStarsToFirstLine(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] lines = text.split("\\r?\\n", 2);
        if (lines.length == 0) {
            return text;
        }

        String firstLine = lines[0];
        String modifiedFirstLine = "*" + firstLine + "*";

        if (lines.length > 1) {
            return modifiedFirstLine + "\n" + lines[1];
        } else {
            return modifiedFirstLine;
        }
    }
}
