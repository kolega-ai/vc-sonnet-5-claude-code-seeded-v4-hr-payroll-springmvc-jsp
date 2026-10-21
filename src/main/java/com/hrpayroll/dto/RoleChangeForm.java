package com.hrpayroll.dto;

import jakarta.validation.constraints.NotBlank;

/** Dedicated, HR-admin-only endpoint DTO for role changes - never bound from the general profile form. */
public class RoleChangeForm {
    @NotBlank
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
