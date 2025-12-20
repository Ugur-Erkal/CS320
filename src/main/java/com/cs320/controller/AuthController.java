package com.cs320.controller;

import com.cs320.controller.dto.LoginRequest;
import com.cs320.controller.dto.RegisterRequest;
import com.cs320.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "login";
    }


    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("pageTitle", "Register");
        return "register";
    }

    @PostMapping("/login")
    public String doLogin(@Valid @ModelAttribute LoginRequest request,
                          BindingResult bindingResult,
                          HttpSession session,
                          RedirectAttributes ra) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse("Validation failed.");
            ra.addFlashAttribute("msg", errorMessage);
            return "redirect:/login";
        }

        var opt = userService.login(request.getUsername(), request.getPassword());

        if (opt.isEmpty()) {
            ra.addFlashAttribute("msg", "Invalid username or password.");
            return "redirect:/login";
        }

        var u = opt.get();
        session.setAttribute("userId", u.getUserId());
        session.setAttribute("userType", u.getUserType());
        session.setAttribute("username", request.getUsername());

        return "redirect:search";
    }


    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute RegisterRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse("Validation failed.");
            ra.addFlashAttribute("msg", errorMessage);
            return "redirect:/register";
        }

        try {
            userService.register(request.getUsername(), request.getPassword(),
                    request.getUserType(), request.getAddress(),
                    request.getPhoneNumber(), request.getCity());
            ra.addFlashAttribute("msg", "Account created. Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("msg", ex.getMessage());
            return "redirect:/register";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }


}
