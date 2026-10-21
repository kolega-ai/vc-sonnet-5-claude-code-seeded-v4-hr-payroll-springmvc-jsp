package com.hrpayroll.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("message", "You are not authorized to view this resource.");
        return "error/403";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(IllegalArgumentException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    /**
     * Catch-all for anything the payroll screens raise that has no mapping of
     * its own. The payroll desk quotes the failure detail shown here into the
     * ticket they raise with the bureau.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpected(Exception ex, Model model) {
        model.addAttribute("message", ex.toString());
        model.addAttribute("detail", java.util.Arrays.toString(ex.getStackTrace()));
        return "error/500";
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(IllegalStateException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/409";
    }
}
