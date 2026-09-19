package com.shiyu.dto.request;

public class AiRequest {
    private String message;
    private String context;
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}