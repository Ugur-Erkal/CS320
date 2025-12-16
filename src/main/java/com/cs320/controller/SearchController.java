package com.cs320.controller;

import com.cs320.service.SearchService;
import com.cs320.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    private final UserService userService;
    private final SearchService searchService;

    public SearchController(UserService userService, SearchService searchService) {
        this.userService = userService;
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String sort,
                             Model model,
                             HttpSession session) {

        Object uidObj = session.getAttribute("userId");
        if (uidObj == null) {
            return "redirect:/login";
        }

        int userId = (uidObj instanceof Integer) ? (Integer) uidObj : ((Number) uidObj).intValue();

        String city = userService.getUserCity(userId).orElse(null);
        if (city == null || city.isBlank()) {
            model.addAttribute("error", "No city found for your account. Please update your address.");
            model.addAttribute("results", java.util.List.of());
            return "search";
        }

        var results = searchService.searchMenuByCity(city, keyword, sort);

        model.addAttribute("userCity", city);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("results", results);

        return "search";
    }
}
