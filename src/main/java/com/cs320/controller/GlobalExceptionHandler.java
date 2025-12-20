package com.cs320.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public RedirectView handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        redirectAttributes.addFlashAttribute("msg", ex.getMessage());
        String redirectPath = resolveRedirectPath(request);
        return new RedirectView(Objects.requireNonNull(redirectPath), true);
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleException(
            Exception ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        redirectAttributes.addFlashAttribute("msg", "An unexpected error occurred. Please try again.");
        String redirectPath = resolveRedirectPath(request);
        return new RedirectView(Objects.requireNonNull(redirectPath), true);
    }

    private String resolveRedirectPath(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        if (requestPath != null && requestPath.contains("/register")) {
            return "/register";
        } else if (requestPath != null && requestPath.contains("/login")) {
            return "/login";
        }
        return "/login";
    }
}

