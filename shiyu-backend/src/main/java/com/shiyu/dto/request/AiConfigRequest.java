package com.shiyu.dto.request;

import java.util.Map;

public class AiConfigRequest {
    private String provider;
    private Map<String, ProviderUpdate> providers;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Map<String, ProviderUpdate> getProviders() { return providers; }
    public void setProviders(Map<String, ProviderUpdate> providers) { this.providers = providers; }

    public static class ProviderUpdate {
        private String type;
        private String apiKey;
        private String apiSecret;
        private String baseUrl;
        private String model;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getApiSecret() { return apiSecret; }
        public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }
}
