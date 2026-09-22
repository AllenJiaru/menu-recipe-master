package com.shiyu.service.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shiyu.config.EmailConfig;
import com.shiyu.entity.AiConfigEntity;
import com.shiyu.mapper.AiConfigMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailConfigPersistence {
    private static final Logger log = LoggerFactory.getLogger(EmailConfigPersistence.class);
    private final AiConfigMapper configMapper;
    private final EmailConfig emailConfig;

    public EmailConfigPersistence(AiConfigMapper configMapper, EmailConfig emailConfig) {
        this.configMapper = configMapper;
        this.emailConfig = emailConfig;
    }

    @PostConstruct
    public void loadFromDb() {
        try {
            String val = configMapper.getValueByKey("email_config");
            if (val != null && !val.isBlank()) {
                JsonObject json = JsonParser.parseString(val).getAsJsonObject();
                if (json.has("enabled")) emailConfig.setEnabled(json.get("enabled").getAsBoolean());
                if (json.has("host")) emailConfig.setHost(json.get("host").getAsString());
                if (json.has("port")) emailConfig.setPort(json.get("port").getAsInt());
                if (json.has("username")) emailConfig.setUsername(json.get("username").getAsString());
                if (json.has("password")) emailConfig.setPassword(json.get("password").getAsString());
                if (json.has("fromName")) emailConfig.setFromName(json.get("fromName").getAsString());
                log.info("Email config loaded from DB: enabled={}, host={}", emailConfig.isEnabled(), emailConfig.getHost());
            }
        } catch (Exception e) {
            log.warn("Failed to load email config from DB: {}", e.getMessage());
        }
    }

    public void save() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", emailConfig.isEnabled());
        json.addProperty("host", emailConfig.getHost());
        json.addProperty("port", emailConfig.getPort());
        json.addProperty("username", emailConfig.getUsername());
        json.addProperty("password", emailConfig.getPassword());
        json.addProperty("fromName", emailConfig.getFromName());

        String value = json.toString();
        try {
            String existing = configMapper.getValueByKey("email_config");
            AiConfigEntity entity = new AiConfigEntity();
            entity.setConfigKey("email_config");
            entity.setConfigValue(value);
            if (existing != null) {
                var e = configMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AiConfigEntity>()
                        .eq("config_key", "email_config"));
                if (e != null) entity.setId(e.getId());
                configMapper.updateById(entity);
            } else {
                configMapper.insert(entity);
            }
        } catch (Exception e) {
            log.error("Failed to save email config: {}", e.getMessage());
        }
    }
}
