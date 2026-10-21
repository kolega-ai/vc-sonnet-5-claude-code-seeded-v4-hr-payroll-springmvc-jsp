package com.hrpayroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Self-service fields only - role, department, manager, status are NOT bindable here. */
public class ProfileUpdateForm {

    @NotBlank
    @Size(max = 30)
    private String phone;

    @Size(max = 255)
    private String address;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
