package com.shiyu.dto.request;

public class AiTranslationRequest {
    private String text;
    private String targetLang;
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getTargetLang() { return targetLang; }
    public void setTargetLang(String targetLang) { this.targetLang = targetLang; }
}