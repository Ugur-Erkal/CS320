package com.cs320.controller;

import com.cs320.service.RatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderRatingController {

    private final RatingService ratingService;

    public OrderRatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/orders/rate")
    public String rateOrder(@RequestParam int cartId,
                            @RequestParam int rating,
                            @RequestParam(required = false) String comment,
                            HttpSession session) {

        Object uidObj = session.getAttribute("userId");
        if (uidObj == null) return "redirect:/login";

        int userId = (uidObj instanceof Integer) ? (Integer) uidObj : ((Number) uidObj).intValue();

        // Basic validation
        if (rating < 1 || rating > 5) {
            // fallback: just go back
            return "redirect:/my-orders";
        }

        ratingService.submitOrderRating(userId, cartId, rating, comment);
        return "redirect:/my-orders";
    }
}
