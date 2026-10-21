package com.hrpayroll.dto;

import jakarta.validation.constraints.NotBlank;

public class PolicyForm {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String audience = "ALL";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
