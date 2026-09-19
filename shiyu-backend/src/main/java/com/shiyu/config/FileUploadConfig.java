package com.shiyu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    private String uploadDir = "./uploads";

    private String allowedTypes = "image/jpeg,image/png,image/gif,image/webp";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(String allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public String getFullPath() {
        return uploadDir;
    }

    public boolean isAllowedType(String contentType) {
        return allowedTypes.contains(contentType);
    }
}