package com.shiyu.dto.response;

public class AiResponse {
    private String content;
    private String provider;
    private long executionTimeMs;
    private Long logId;
    public AiResponse() {}
    public AiResponse(String content, String provider, long executionTimeMs) {
        this.content = content; this.provider = provider; this.executionTimeMs = executionTimeMs;
    }
    public AiResponse(String content, String provider, long executionTimeMs, Long logId) {
        this.content = content; this.provider = provider; this.executionTimeMs = executionTimeMs; this.logId = logId;
    }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
}
