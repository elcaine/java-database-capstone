package com.example.cliniccapstone.controller;

import com.example.cliniccapstone.dto.Login;
import com.example.cliniccapstone.model.Patient;
import com.example.cliniccapstone.service.PatientService;
import com.example.cliniccapstone.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * PatientController
 * REST controller for patient operations: signup, login, details, appointments, and filtering.
 */
@RestController
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private Service service;

    /**
     * Get patient details using token (patient token required).
     */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        return patientService.getPatientDetails(token);
    }

    /**
     * Create a new patient (signup).
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        Map<String, String> res = new HashMap<>();

        boolean okToCreate = service.validatePatient(patient);
        if (!okToCreate) {
            res.put("message", "Patient with email id or phone no already exist");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }

        // NOTE: method name must match your PatientService implementation.
        // If your course template uses createPatient(patient), keep it.
        int created = patientService.createPatient(patient);

        if (created == 1) {
            res.put("message", "Signup successful");
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } else if (created == -1) {
            // Optional: if your create method distinguishes duplicates again
            res.put("message", "Patient with email id or phone no already exist");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        } else {
            res.put("message", "Internal server error");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Patient login.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    /**
     * Get patient appointments (patient token required).
     */
    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointments(
            @PathVariable Long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        // NOTE: method name must match your PatientService implementation.
        // If your course template says getPatientAppointment(id, token), use that.
        return patientService.getPatientAppointments(id, token);
    }

    /**
     * Filter patient appointments (patient token required).
     */
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointments(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        return service.filterPatient(condition, name, token);
    }
}