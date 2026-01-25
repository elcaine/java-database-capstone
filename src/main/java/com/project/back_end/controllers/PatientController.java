package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.service.ClinicService;
import com.project.back_end.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final ClinicService service;

    public PatientController(PatientService patientService, ClinicService service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.putAll(tokenRes.getBody() == null ? Map.of("message", "Unauthorized") : tokenRes.getBody());
            return new ResponseEntity<>(err, tokenRes.getStatusCode());
        }
        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPatient(@Valid @RequestBody Patient patient) {
        boolean okToCreate = service.validatePatient(patient);
        if (!okToCreate) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Patient with email id or phone no already exist");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }
        return patientService.createPatient(patient);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    @GetMapping("/{id}/{token}/{user}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            @PathVariable Long id,
            @PathVariable String token,
            @PathVariable String user
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, user);
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.putAll(tokenRes.getBody() == null ? Map.of("message", "Unauthorized") : tokenRes.getBody());
            return new ResponseEntity<>(err, tokenRes.getStatusCode());
        }
        // Method name here should match your PatientService implementation.
        // If your service uses getPatientAppointments(...), keep it; otherwise rename to getPatientAppointment(...)
        return patientService.getPatientAppointments(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.putAll(tokenRes.getBody() == null ? Map.of("message", "Unauthorized") : tokenRes.getBody());
            return new ResponseEntity<>(err, tokenRes.getStatusCode());
        }
        return service.filterPatient(condition, name, token);
    }
}
