package com.cs320.controller;

import com.cs320.service.DiscountService;
import com.cs320.service.ManagerService;
import com.cs320.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class DiscountController {

    private final DiscountService discountService;
    private final ManagerService managerService;
    private final UserService userService;

    public DiscountController(DiscountService discountService,
                              ManagerService managerService,
                              UserService userService) {
        this.discountService = discountService;
        this.managerService = managerService;
        this.userService = userService;
    }

    @GetMapping("/manager/discounts")
    public String discounts(@RequestParam int restaurantId, Model model, HttpSession session) {
        Object uidObj = session.getAttribute("userId");
        if (uidObj == null) return "redirect:/login";

        int userId = (uidObj instanceof Integer) ? (Integer) uidObj : ((Number) uidObj).intValue();

        // 1) manager mı?
        String userType = userService.getUserType(userId).orElse(null);
        if (userType == null || !userType.equalsIgnoreCase("Manager")) {
            return "redirect:/home";
        }

        // 2) bu restaurant'ı yönetiyor mu?
        if (!managerService.managesRestaurant(userId, restaurantId)) {
            model.addAttribute("pageTitle", "Discount Management");
            model.addAttribute("error", "You are not authorized to manage discounts for this restaurant.");
            model.addAttribute("restaurantId", restaurantId);
            model.addAttribute("menuItems", java.util.List.of());
            model.addAttribute("discounts", java.util.List.of());
            return "discounts";
        }

        model.addAttribute("pageTitle", "Discount Management");
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("menuItems", discountService.getMenuItemsForRestaurant(restaurantId));
        model.addAttribute("discounts", discountService.getDiscountsForRestaurant(restaurantId));

        return "discounts";
    }

    @PostMapping("/manager/discounts/apply")
    public String applyDiscount(@RequestParam int restaurantId,
                                @RequestParam int menuItemId,
                                @RequestParam double discountPercent,
                                @RequestParam String start,
                                @RequestParam String end,
                                HttpSession session,
                                Model model) {

        Object uidObj = session.getAttribute("userId");
        if (uidObj == null) return "redirect:/login";

        int userId = (uidObj instanceof Integer) ? (Integer) uidObj : ((Number) uidObj).intValue();

        String userType = userService.getUserType(userId).orElse(null);
        if (userType == null || !userType.equalsIgnoreCase("Manager")) {
            return "redirect:/home";
        }

        if (!managerService.managesRestaurant(userId, restaurantId)) {
            return "redirect:/dashboard";
        }

        // ekstra güvenlik: menuItem gerçekten o restaurant'a mı ait?
        if (!discountService.menuItemBelongsToRestaurant(menuItemId, restaurantId)) {
            model.addAttribute("pageTitle", "Discount Management");
            model.addAttribute("restaurantId", restaurantId);
            model.addAttribute("menuItems", discountService.getMenuItemsForRestaurant(restaurantId));
            model.addAttribute("discounts", discountService.getDiscountsForRestaurant(restaurantId));
            model.addAttribute("error", "Selected menu item does not belong to this restaurant.");
            return "discounts";
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime startDt = LocalDateTime.parse(start, fmt);
        LocalDateTime endDt = LocalDateTime.parse(end, fmt);

        discountService.applyDiscount(menuItemId, discountPercent, startDt, endDt);

        return "redirect:/manager/discounts?restaurantId=" + restaurantId;
    }
}
