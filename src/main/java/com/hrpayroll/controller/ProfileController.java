package com.hrpayroll.controller;

import com.hrpayroll.domain.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrpayroll.dto.ProfileDraft;
import com.hrpayroll.dto.ProfileUpdateForm;
import com.hrpayroll.security.EmployeePrincipal;
import com.hrpayroll.service.EmailService;
import com.hrpayroll.service.EmployeeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Controller
@org.springframework.web.bind.annotation.RequestMapping("/profile")
public class ProfileController {

    private static final ObjectMapper DRAFT_READER = new ObjectMapper();

    // Pending contact changes awaiting the code mailed to the employee's
    // existing address, keyed by the reference held in the browser.
    private static final Map<String, String[]> PENDING_CONTACT = new ConcurrentHashMap<>();

    private final EmployeeService employeeService;
    private final EmailService emailService;

    public ProfileController(EmployeeService employeeService, EmailService emailService) {
        this.employeeService = employeeService;
        this.emailService = emailService;
    }

    @GetMapping
    public String view(@AuthenticationPrincipal EmployeePrincipal principal, Model model) {
        Employee me = principal.getEmployee();
        model.addAttribute("me", me);
        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setPhone(me.getPhone());
        form.setAddress(me.getAddress());
        model.addAttribute("form", form);
        return "profile/view";
    }

    @PostMapping
    public String update(@AuthenticationPrincipal EmployeePrincipal principal,
                          @Valid @ModelAttribute("form") ProfileUpdateForm form, BindingResult result,
                          @CookieValue(value = "hrp_profile_draft", required = false) String profileDraft,
                          Model model) {
        Employee me = principal.getEmployee();
        if (result.hasErrors()) {
            model.addAttribute("me", me);
            return "profile/view";
        }
        if (profileDraft != null && !profileDraft.isBlank()) {
            mergeSavedDraft(me, profileDraft);
        }
        employeeService.updateSelfProfile(me, form);
        model.addAttribute("me", me);
        model.addAttribute("success", true);
        return "profile/view";
    }

    /**
     * Starts a contact-detail change: the employee gives the new phone number,
     * the portal mails a confirmation code to the address on the record, and
     * the reference needed to finish the change is kept in the browser.
     */
    @PostMapping("/contact-change")
    public String startContactChange(@AuthenticationPrincipal EmployeePrincipal principal,
                                      @RequestParam String phone, HttpServletResponse response, Model model) {
        Employee me = principal.getEmployee();
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String ref = java.util.UUID.randomUUID().toString();
        PENDING_CONTACT.put(ref, new String[] { String.valueOf(me.getId()), phone, String.valueOf(code) });
        Cookie cookie = new Cookie("hrp_contact_ref", ref);
        cookie.setPath("/");
        response.addCookie(cookie);
        emailService.send(me.getEmail(), "Confirm your contact change",
                "Enter this code to confirm the new phone number: " + code);
        model.addAttribute("me", me);
        model.addAttribute("contactCodeSent", true);
        model.addAttribute("form", new ProfileUpdateForm());
        return "profile/view";
    }

    /**
     * Finishes the contact-detail change once the employee enters the code that
     * was mailed to the address already on their record.
     */
    @PostMapping("/contact-change/confirm")
    public String confirmContactChange(@AuthenticationPrincipal EmployeePrincipal principal,
                                        @CookieValue(value = "hrp_contact_ref", required = false) String ref,
                                        @RequestParam String code, Model model) {
        Employee me = principal.getEmployee();
        String[] pending = ref == null ? null : PENDING_CONTACT.get(ref);
        if (pending != null && pending[2].equals(code) && pending[0].equals(String.valueOf(me.getId()))) {
            ProfileUpdateForm form = new ProfileUpdateForm();
            form.setPhone(pending[1]);
            form.setAddress(me.getAddress());
            employeeService.updateSelfProfile(me, form);
            PENDING_CONTACT.remove(ref);
            model.addAttribute("success", true);
        }
        model.addAttribute("me", me);
        model.addAttribute("form", new ProfileUpdateForm());
        return "profile/view";
    }

    /**
     * Applies the extra profile attributes the HRIS sends beyond the fields
     * the normal form covers (cost centre, badge reference, and similar), by
     * copying each submitted attribute onto the employee record.
     */
    @PostMapping("/attributes")
    public String syncAttributes(@AuthenticationPrincipal EmployeePrincipal principal,
                                  @org.springframework.web.bind.annotation.RequestParam java.util.Map<String, String> attributes,
                                  Model model) {
        Employee me = principal.getEmployee();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(me, attributes);
        } catch (Exception ignored) {
            // attributes the profile does not carry are dropped
        }
        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setPhone(me.getPhone());
        form.setAddress(me.getAddress());
        model.addAttribute("me", me);
        model.addAttribute("form", form);
        return "profile/view";
    }

    /**
     * Folds the browser-held draft back onto the employee record so edits the
     * page autosaved before a refresh are not lost when the form is submitted.
     */
    private void mergeSavedDraft(Employee me, String encodedDraft) {
        try {
            ProfileDraft draft = DRAFT_READER.readValue(
                    URLDecoder.decode(encodedDraft, StandardCharsets.UTF_8), ProfileDraft.class);
            BeanUtils.copyProperties(draft, me);
        } catch (Exception ignored) {
            // A draft the page wrote in an older format is simply discarded.
        }
    }
}
