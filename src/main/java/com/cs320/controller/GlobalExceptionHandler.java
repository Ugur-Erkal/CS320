package com.cs320.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public RedirectView handleBindException(
            BindException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        ex.printStackTrace();
        System.out.println("GlobalExceptionHandler caught: " + ex.getClass().getName() + " uri=" + request.getRequestURI());
        
        String errorMessage = ex.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed.");
        redirectAttributes.addFlashAttribute("msg", errorMessage);
        String redirectPath = resolveRedirectPath(request);
        return new RedirectView(Objects.requireNonNull(redirectPath), true);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public RedirectView handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        ex.printStackTrace();
        System.out.println("GlobalExceptionHandler caught: " + ex.getClass().getName() + " uri=" + request.getRequestURI());
        
        redirectAttributes.addFlashAttribute("msg", ex.getMessage());
        String redirectPath = resolveRedirectPath(request);
        return new RedirectView(Objects.requireNonNull(redirectPath), true);
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleException(
            Exception ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        ex.printStackTrace();
        System.out.println("GlobalExceptionHandler caught: " + ex.getClass().getName() + " uri=" + request.getRequestURI());
        
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

