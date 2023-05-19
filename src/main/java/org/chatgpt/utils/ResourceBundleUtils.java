package org.chatgpt.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleUtils {

    public ResourceBundleUtils() {
    }

    public static String getTranslate(String key, String lang) {
        ResourceBundle bundle = ResourceBundle.getBundle("Translations", new Locale(lang));
        return bundle.getString(key);
    }
    public static String getGeneralProperties(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("general");
        return bundle.getString(key);
    }
}
