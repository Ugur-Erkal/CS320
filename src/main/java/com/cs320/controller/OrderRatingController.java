package com.cs320.controller;

import com.cs320.service.RatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                            HttpSession session,
                            RedirectAttributes ra) {

        Object uidObj = session.getAttribute("userId");
        if (uidObj == null) return "redirect:/login";

        int userId = (uidObj instanceof Integer) ? (Integer) uidObj : ((Number) uidObj).intValue();

        if (rating < 1 || rating > 5) {
            ra.addFlashAttribute("msg", "Rating must be between 1 and 5.");
            return "redirect:/my-orders";
        }

        try {
            ratingService.submitOrderRating(userId, cartId, rating, comment);
            ra.addFlashAttribute("msg", "Review submitted.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("msg", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            ra.addFlashAttribute("msg", "Could not submit review.");
        }

        return "redirect:/my-orders";
    }
}
