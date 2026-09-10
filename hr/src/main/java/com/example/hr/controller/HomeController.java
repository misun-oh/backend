package com.example.hr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.hr.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DashboardService dashboardService;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        return "hr/dashboard";
    }
}
