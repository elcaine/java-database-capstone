package com.project.back_end.controller;

import com.project.back_end.models.Admin;
import com.project.back_end.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AdminController
 * REST controller for admin authentication.
 */
@RestController
@RequestMapping("${api.path}" + "admin")
public class AdminController {

    @Autowired
    private ClinicService service;

    /**
     * Admin login endpoint.
     * Accepts Admin credentials in the request body and returns a token if valid.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return service.validateAdmin(admin);
    }
}