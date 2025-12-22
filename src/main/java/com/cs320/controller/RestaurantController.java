package com.cs320.controller;

import com.cs320.service.RestaurantService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/restaurant/{id}")
    public String restaurantDetail(@PathVariable("id") int restaurantId,
                                   @RequestParam(name = "q", required = false) String keyword,
                                   HttpSession session,
                                   Model model) {

        // login check (customer side)
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        var opt = restaurantService.getRestaurantDetail(restaurantId, keyword);
        if (opt.isEmpty()) {
            model.addAttribute("error", "Restaurant not found.");
            return "restaurant-detail";
        }

        model.addAttribute("restaurant", opt.get());
        model.addAttribute("q", keyword);
        return "restaurant-detail";
    }
}
