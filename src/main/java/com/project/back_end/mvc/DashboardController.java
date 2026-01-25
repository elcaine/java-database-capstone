package com.project.back_end.mvc;

import com.project.back_end.service.ClinicService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    private final ClinicService service;

    public DashboardController(ClinicService service) {
        this.service = service;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        // Shared service validates token for "admin" role.
        // If valid, the controller should forward to the admin dashboard view.
        // If invalid, redirect to root.
        var tokenRes = service.validateToken(token, "admin");
        Map<String, String> body = tokenRes.getBody();

        if (tokenRes.getStatusCode().is2xxSuccessful() && (body == null || body.isEmpty())) {
            return "admin/adminDashboard";
        }

        return "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        var tokenRes = service.validateToken(token, "doctor");
        Map<String, String> body = tokenRes.getBody();

        if (tokenRes.getStatusCode().is2xxSuccessful() && (body == null || body.isEmpty())) {
            return "doctor/doctorDashboard";
        }

        return "redirect:/";
    }
}
