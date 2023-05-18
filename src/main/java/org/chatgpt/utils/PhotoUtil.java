package org.chatgpt.utils;

import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.nio.file.Paths;

public class PhotoUtil {

    public PhotoUtil() {
    }

    public static InputFile getInputFileByPath(String imageUrl) {
        return new InputFile(Paths.get(imageUrl).toFile());
    }
}
