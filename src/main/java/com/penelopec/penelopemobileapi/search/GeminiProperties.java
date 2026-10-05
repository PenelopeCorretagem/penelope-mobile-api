package com.penelopec.penelopemobileapi.search;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai-search.gemini")
public record GeminiProperties(String apiKey, String model) {
    public GeminiProperties {
        model = model == null || model.isBlank() ? "gemini-3.6-flash" : model;
    }
}
