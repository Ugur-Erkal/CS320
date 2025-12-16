package com.cs320.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    @GetMapping("/search")
    public String searchPage(
            @RequestParam(required = false) String city,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String sort,
            Model model
    ) {
        model.addAttribute("pageTitle", "Search");
        model.addAttribute("city", city);
        model.addAttribute("q", query);
        model.addAttribute("sort", sort);
        return "search";
    }
}
