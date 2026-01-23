package com.project.back_end.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TokenValidationService {

    public Map<String, Object> validateToken(String token, String role) {
        Map<String, Object> errors = new HashMap<>();

        if (token == null || token.isBlank()) {
            errors.put("error", "Missing token");
            return errors;
        }

        if (role == null || role.isBlank()) {
            errors.put("error", "Missing role");
            return errors;
        }

        return Map.of();
    }
}
