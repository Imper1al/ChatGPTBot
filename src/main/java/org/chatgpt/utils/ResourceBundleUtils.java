package org.chatgpt.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleUtils {

    private static String languageCode = "en";

    public ResourceBundleUtils() {
    }

    public static String getTranslate(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("Translations", new Locale(languageCode));
        return bundle.getString(key);
    }
    public static String getGeneralProperties(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("general");
        return bundle.getString(key);
    }

    public static void setLanguageCode(String languageCode) {
        ResourceBundleUtils.languageCode = languageCode;
    }

    public static String getLanguageCode() {
        return languageCode;
    }
}
