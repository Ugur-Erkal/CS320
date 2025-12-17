package com.cs320.controller;

import com.cs320.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

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
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes ra) {

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            ra.addFlashAttribute("msg", "Username and password are required.");
            return "redirect:/login";
        }

        var opt = userService.login(username, password);

        if (opt.isEmpty()) {
            ra.addFlashAttribute("msg", "Invalid username or password.");
            return "redirect:/login";
        }

        var u = opt.get();
        session.setAttribute("userId", u.getUserId());
        session.setAttribute("userType", u.getUserType());
        session.setAttribute("username", username);

        return "redirect:search";
    }


    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String userType,
                             @RequestParam String address,
                             @RequestParam String phoneNumber,
                             @RequestParam String city,
                             RedirectAttributes ra) {
        try {
            userService.register(username, password, userType, address, phoneNumber, city);
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
