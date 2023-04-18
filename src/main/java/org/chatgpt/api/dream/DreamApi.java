package org.chatgpt.api.dream;

import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.net.HttpURLConnection;
import java.util.List;

public class DreamApi {
    private static final String API_KEY = "vDbdhIrB85TvQ8lCIrBi49nouJ9i6NBt";

    private final DreamImage dreamImage;
    private final DreamStyles dreamStyles;

    public DreamApi() {
        this.dreamImage = new DreamImage(API_KEY);
        this.dreamStyles = new DreamStyles();
    }

    public InputFile generateImages(String styleId, String description) {
        HttpURLConnection httpURLConnection = dreamImage.apiRequest(styleId, description);
        return dreamImage.getResponse(httpURLConnection);
    }

    public List<String> getStyles() {
        return dreamStyles.getStyles();
    }
}
