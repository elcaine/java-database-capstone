package com.project.back_end.controller;

import com.project.back_end.service.TokenValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * DashboardController
 * Maps requests to Thymeleaf templates based on user roles and tokens.
 */
@Controller
public class DashboardController {

    @Autowired
    private TokenValidationService tokenValidationService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        Map<String, Object> result = tokenValidationService.validateToken(token, "admin");

        if (result == null || result.isEmpty()) {
            return "admin/adminDashboard";
        }

        return "redirect:http://localhost:8080/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        Map<String, Object> result = tokenValidationService.validateToken(token, "doctor");

        if (result == null || result.isEmpty()) {
            return "doctor/doctorDashboard";
        }

        return "redirect:http://localhost:8080/";
    }
}
