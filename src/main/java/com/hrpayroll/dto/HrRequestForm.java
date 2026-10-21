package com.hrpayroll.dto;

import jakarta.validation.constraints.NotBlank;

public class HrRequestForm {
    @NotBlank
    private String subject;

    @NotBlank
    private String description;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
