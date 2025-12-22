package com.cs320.controller;

import com.cs320.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private Integer getUserId(HttpSession session) {
        Object uidObj = session.getAttribute("userId");
        if (uidObj == null) return null;
        return (uidObj instanceof Integer) ? (Integer) uidObj : ((Number) uidObj).intValue();
    }

    @GetMapping("/cart")
    public String cartPage(Model model, HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return "redirect:/login";

        model.addAttribute("pageTitle", "Cart");
        model.addAttribute("items", cartService.getCartItems(userId));
        model.addAttribute("total", cartService.getCartTotal(userId));
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("menuItemId") int menuItemId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return "redirect:/login";

        if (quantity <= 0) quantity = 1;

        cartService.addToCart(userId, menuItemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("menuItemId") int menuItemId,
                                 HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return "redirect:/login";

        cartService.removeFromCart(userId, menuItemId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session, Model model) {
        Integer userId = getUserId(session);
        if (userId == null) return "redirect:/login";

        try {
            cartService.checkout(userId);
            return "redirect:/my-orders";
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Cart");
            model.addAttribute("error", e.getMessage());
            model.addAttribute("items", cartService.getCartItems(userId));
            model.addAttribute("total", cartService.getCartTotal(userId));
            return "cart";
        }
    }
}
