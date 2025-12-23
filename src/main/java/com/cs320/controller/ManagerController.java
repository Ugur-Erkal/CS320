package com.cs320.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // ----------------------------
    // Existing dashboard (unchanged + 1 new button)
    // ----------------------------
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(name = "restaurantId", required = false) Integer restaurantId,
                            Model model,
                            HttpSession session) {

        Integer managerId = requireManagerId(session);
        if (managerId == null) return "redirect:/login";

        var restaurants = managerService.getManagedRestaurants(managerId);
        model.addAttribute("pageTitle", "Restaurant Dashboard");
        model.addAttribute("restaurants", restaurants);

        if (restaurants.isEmpty()) {
            model.addAttribute("selectedRestaurantId", null);
            model.addAttribute("orders", java.util.List.of());
            // NOTE: msg'yi flash yerine model'e koyduk (daha stabil)
            model.addAttribute("msg", "No restaurant is assigned to this manager.");
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

    // ----------------------------
    // NEW: Create restaurant pages
    // ----------------------------
    @GetMapping("/manager/restaurants/new")
    public String createRestaurantPage(Model model, HttpSession session) {
        Integer managerId = requireManagerId(session);
        if (managerId == null) return "redirect:/login";

        model.addAttribute("pageTitle", "Create Restaurant");
        return "restaurant-create";
    }

    @PostMapping("/manager/restaurants")
    public String createRestaurant(@RequestParam String restaurantName,
                                   @RequestParam String address,
                                   @RequestParam String city,
                                   @RequestParam(required = false) String cuisineType,
                                   HttpSession session) {

        Integer managerId = requireManagerId(session);
        if (managerId == null) return "redirect:/login";

        if (isBlank(restaurantName) || isBlank(address) || isBlank(city)) {
            return "redirect:/manager/restaurants/new";
        }

        managerService.createRestaurant(
                managerId,
                restaurantName.trim(),
                address.trim(),
                city.trim(),
                cuisineType == null ? null : cuisineType.trim()
        );

        return "redirect:/dashboard";
    }

    // ----------------------------
    // NEW: Manage restaurant page (menu + keywords)
    // ----------------------------
    @GetMapping("/manager/restaurants/{restaurantId}")
    public String manageRestaurant(@PathVariable int restaurantId,
                                   Model model,
                                   HttpSession session) {

        Integer managerId = requireManagerId(session);
        if (managerId == null) return "redirect:/login";

        if (!managerService.managesRestaurant(managerId, restaurantId)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("pageTitle", "Manage Restaurant");
        model.addAttribute("restaurant", managerService.getRestaurantDetails(restaurantId));
        model.addAttribute("menuItems", managerService.getMenuItemsForRestaurant(restaurantId));
        model.addAttribute("keywords", managerService.getKeywordsForRestaurant(restaurantId));
        return "restaurant-manage";
    }

    @PostMapping("/manager/restaurants/{restaurantId}/menu-items")
    public String addMenuItem(@PathVariable int restaurantId,
                              @RequestParam String name,
                              @RequestParam BigDecimal price,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String imageUrl,
                              HttpSession session) {

        Integer managerId = requireManagerId(session);
        if (managerId == null) return "redirect:/login";
        if (!managerService.managesRestaurant(managerId, restaurantId)) return "redirect:/dashboard";

        if (isBlank(name) || price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return "redirect:/manager/restaurants/" + restaurantId;
        }

        managerService.addMenuItemToRestaurant(
                managerId,
                restaurantId,
                name.trim(),
                price,
                (description == null ? null : description.trim()),
                (imageUrl == null ? null : imageUrl.trim())
        );

        return "redirect:/manager/restaurants/" + restaurantId;
    }

    @PostMapping("/manager/restaurants/{restaurantId}/keywords")
    public String addKeyword(@PathVariable int restaurantId,
                             @RequestParam String keyword,
                             HttpSession session) {

        Integer managerId = requireManagerId(session);
        if (managerId == null) return "redirect:/login";
        if (!managerService.managesRestaurant(managerId, restaurantId)) return "redirect:/dashboard";

        if (isBlank(keyword)) {
            return "redirect:/manager/restaurants/" + restaurantId;
        }

        managerService.addKeywordToRestaurant(managerId, restaurantId, keyword.trim().toLowerCase());
        return "redirect:/manager/restaurants/" + restaurantId;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
