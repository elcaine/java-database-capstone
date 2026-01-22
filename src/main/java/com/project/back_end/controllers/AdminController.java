package com.example.cliniccapstone.controller;

import com.example.cliniccapstone.model.Admin;
import com.example.cliniccapstone.service.Service;
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
    private Service service;

    /**
     * Admin login endpoint.
     * Accepts Admin credentials in the request body and returns a token if valid.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return service.validateAdmin(admin);
    }
}