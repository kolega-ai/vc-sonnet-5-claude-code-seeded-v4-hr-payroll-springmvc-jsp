package com.hrpayroll.dto;

import com.hrpayroll.domain.EmploymentStatus;

/**
 * Unsent profile edits the portal page keeps client-side so a half-finished
 * change survives a browser refresh. The page writes it as JSON into the
 * hrp_profile_draft cookie and the server folds it back in on the next save.
 */
public class ProfileDraft {

    private String phone;
    private String address;
    private String jobTitle;
    private EmploymentStatus employmentStatus;

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

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }
}
