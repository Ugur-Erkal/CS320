package com.cs320.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cs320.service.ManagerService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    private Integer requireManagerId(HttpSession session) {
        Object uidObj = session.getAttribute("userId");
        Object typeObj = session.getAttribute("userType");
        if (uidObj == null || typeObj == null) return null;
        if (!"Manager".equalsIgnoreCase(String.valueOf(typeObj))) return null;
        return (uidObj instanceof Integer) ? (Integer) uidObj : ((Number) uidObj).intValue();
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(name = "restaurantId", required = false) Integer restaurantId,
                            Model model,
                            HttpSession session,
                            RedirectAttributes ra) {

        Integer managerId = requireManagerId(session);
        if (managerId == null) return "redirect:/login";

        var restaurants = managerService.getManagedRestaurants(managerId);
        model.addAttribute("pageTitle", "Restaurant Dashboard");
        model.addAttribute("restaurants", restaurants);

        if (restaurants.isEmpty()) {
            model.addAttribute("selectedRestaurantId", null);
            model.addAttribute("orders", java.util.List.of());
            ra.addFlashAttribute("msg", "No restaurant is assigned to this manager.");
            return "manager-dashboard";
        }

        int selectedId = (restaurantId != null) ? restaurantId : restaurants.get(0).restaurantId();
        model.addAttribute("selectedRestaurantId", selectedId);
        model.addAttribute("orders", managerService.getIncomingOrdersForRestaurant(selectedId));
        return "manager-dashboard";
    }

    @PostMapping("/accept-order/{cartId}")
    public String acceptOrder(@PathVariable int cartId,
                              @RequestParam(name = "restaurantId", required = false) Integer restaurantId,
                              HttpSession session) {

        Integer managerId = requireManagerId(session);
        if (managerId == null) return "redirect:/login";

        managerService.acceptOrder(cartId);
        return (restaurantId != null)
                ? "redirect:/dashboard?restaurantId=" + restaurantId
                : "redirect:/dashboard";
    }
}

