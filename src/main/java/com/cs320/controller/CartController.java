package com.cs320.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    @GetMapping("/cart")
    public String cartPage(Model model) {
        model.addAttribute("pageTitle", "Cart");
        return "cart";
    }
}


