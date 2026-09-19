package com.shiyu.service.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shiyu.config.AiConfig;
import com.shiyu.entity.AiConfigEntity;
import com.shiyu.mapper.AiConfigMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiConfigPersistence {
    private static final Logger log = LoggerFactory.getLogger(AiConfigPersistence.class);
    private final AiConfigMapper configMapper;
    private final AiConfig aiConfig;
    private final Gson gson = new Gson();

    public AiConfigPersistence(AiConfigMapper configMapper, AiConfig aiConfig) {
        this.configMapper = configMapper;
        this.aiConfig = aiConfig;
    }

    @PostConstruct
    public void loadFromDb() {
        try {
            String providerVal = configMapper.getValueByKey("provider");
            if (providerVal != null && !providerVal.isBlank()) {
                aiConfig.setProvider(providerVal);
            }
            String providersVal = configMapper.getValueByKey("providers");
            if (providersVal != null && !providersVal.isBlank()) {
                JsonObject json = JsonParser.parseString(providersVal).getAsJsonObject();
                Map<String, AiConfig.Provider> providers = new HashMap<>();
                json.entrySet().forEach(entry -> {
                    AiConfig.Provider p = new AiConfig.Provider();
                    JsonObject pv = entry.getValue().getAsJsonObject();
                    if (pv.has("type")) p.setType(pv.get("type").getAsString());
                    if (pv.has("apiKey")) p.setApiKey(pv.get("apiKey").getAsString());
                    if (pv.has("apiSecret")) p.setApiSecret(pv.get("apiSecret").getAsString());
                    if (pv.has("baseUrl")) p.setBaseUrl(pv.get("baseUrl").getAsString());
                    if (pv.has("model")) p.setModel(pv.get("model").getAsString());
                    providers.put(entry.getKey(), p);
                });
                aiConfig.setProviders(providers);
            }
            log.info("AI config loaded from DB: provider={}", aiConfig.getProvider());
        } catch (Exception e) {
            log.warn("Failed to load AI config from DB, using defaults: {}", e.getMessage());
        }
    }

    public void saveProvider(String provider) {
        upsert("provider", provider);
    }

    public void saveProviders(Map<String, AiConfig.Provider> providers) {
        JsonObject json = new JsonObject();
        providers.forEach((id, p) -> {
            JsonObject pv = new JsonObject();
            pv.addProperty("type", p.getType() != null ? p.getType() : "openai-compatible");
            pv.addProperty("apiKey", p.getApiKey() != null ? p.getApiKey() : "");
            pv.addProperty("apiSecret", p.getApiSecret() != null ? p.getApiSecret() : "");
            pv.addProperty("baseUrl", p.getBaseUrl() != null ? p.getBaseUrl() : "");
            pv.addProperty("model", p.getModel() != null ? p.getModel() : "");
            json.add(id, pv);
        });
        upsert("providers", json.toString());
    }

    private void upsert(String key, String value) {
        try {
            String existing = configMapper.getValueByKey(key);
            AiConfigEntity entity = new AiConfigEntity();
            entity.setConfigKey(key);
            entity.setConfigValue(value);
            if (existing != null) {
                entity.setId(getIdByKey(key));
                configMapper.updateById(entity);
            } else {
                configMapper.insert(entity);
            }
        } catch (Exception e) {
            log.error("Failed to save AI config key={}: {}", key, e.getMessage());
        }
    }

    private Long getIdByKey(String key) {
        try {
            var entity = configMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AiConfigEntity>()
                    .eq("config_key", key)
            );
            return entity != null ? entity.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
