package com.cs320.controller;

import com.cs320.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("items", cartService.getActiveCartItems(userId));
        model.addAttribute("total", cartService.getActiveCartTotal(userId));
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("menuItemId") int menuItemId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return "redirect:/login";

        cartService.addToCart(userId, menuItemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("cartId") int cartId,
                                 @RequestParam("menuItemId") int menuItemId,
                                 HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return "redirect:/login";

        cartService.removeFromCart(userId, cartId, menuItemId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam("cartId") int cartId,
                           HttpSession session,
                           Model model) {
        Integer userId = getUserId(session);
        if (userId == null) return "redirect:/login";

        try {
            cartService.checkoutCart(userId, cartId);
            return "redirect:/my-orders";
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Cart");
            model.addAttribute("error", e.getMessage());
            model.addAttribute("items", cartService.getActiveCartItems(userId));
            model.addAttribute("total", cartService.getActiveCartTotal(userId));
            return "cart";
        }
    }
}
