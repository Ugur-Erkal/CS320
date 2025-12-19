package com.cs320.controller;

import com.cs320.service.OrderHistoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyOrdersController {

    private final OrderHistoryService orderHistoryService;

    public MyOrdersController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model, HttpSession session) {
        Object uidObj = session.getAttribute("userId");
        if (uidObj == null) return "redirect:/login";

        int userId = (uidObj instanceof Integer) ? (Integer) uidObj : ((Number) uidObj).intValue();

        model.addAttribute("pageTitle", "My Orders");
        model.addAttribute("orders", orderHistoryService.getOrdersForUser(userId));
        return "my-orders";
    }
}

