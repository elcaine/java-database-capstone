package com.project.back_end.controller;

import com.project.back_end.dto.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.service.PatientService;
import com.project.back_end.service.ClinicService;
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
    private ClinicService service;

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
    public ResponseEntity<Map<String, Object>> createPatient(@RequestBody Patient patient) {

        // Keep your existing pre-check (if you want it)
        boolean okToCreate = service.validatePatient(patient);
        if (!okToCreate) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Patient with email id or phone no already exist");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }

        // IMPORTANT: PatientService.createPatient(...) returns a ResponseEntity, not int
        return patientService.createPatient(patient);
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