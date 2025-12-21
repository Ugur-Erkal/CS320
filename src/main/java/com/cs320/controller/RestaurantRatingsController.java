package com.cs320.controller;

import com.cs320.service.RatingService;
import com.cs320.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RestaurantRatingsController {

    private final RatingService ratingService;
    private final UserService userService;

    public RestaurantRatingsController(RatingService ratingService, UserService userService) {
        this.ratingService = ratingService;
        this.userService = userService;
    }

    @GetMapping("/restaurants/{id}/ratings")
    public String showRatings(@PathVariable("id") int restaurantId,
                              @RequestParam(value = "error", required = false) String error,
                              Model model) {

        var ratings = ratingService.getRatingsForRestaurant(restaurantId);
        var avg = ratingService.getAverageRatingForRestaurant(restaurantId);

        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("ratings", ratings);
        model.addAttribute("avgRating", avg);
        model.addAttribute("error", error);

        return "restaurant-ratings";
    }

    @PostMapping("/restaurants/{id}/ratings")
    public String submitRating(@PathVariable("id") int restaurantId,
                               @RequestParam("rating") int rating,
                               @RequestParam(value = "comment", required = false) String comment,
                               @RequestParam("username") String username,
                               @RequestParam("password") String password) {

        // Basit validasyon
        if (rating < 1 || rating > 5) {
            return "redirect:/restaurants/" + restaurantId + "/ratings?error=Rating must be between 1 and 5";
        }

        // Kullanıcıyı doğrula (session yapısı yoksa, minimum çözüm)
        var opt = userService.login(username, password);
        if (opt.isEmpty()) {
            return "redirect:/restaurants/" + restaurantId + "/ratings?error=Invalid username/password";
        }

        int userId = opt.get().getUserId();

        String safeComment = (comment == null || comment.isBlank()) ? null : comment.trim();

        ratingService.addRating(restaurantId, userId, rating, safeComment);

        return "redirect:/restaurants/" + restaurantId + "/ratings";
    }
}
