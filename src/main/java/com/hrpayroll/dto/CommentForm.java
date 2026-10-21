package com.hrpayroll.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentForm {
    @NotBlank
    private String body;

    private boolean internalOnly;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isInternalOnly() {
        return internalOnly;
    }

    public void setInternalOnly(boolean internalOnly) {
        this.internalOnly = internalOnly;
    }
}
