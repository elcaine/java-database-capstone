package com.example.cliniccapstone.controller;

import com.example.cliniccapstone.model.Prescription;
import com.example.cliniccapstone.service.PrescriptionService;
import com.example.cliniccapstone.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PrescriptionController
 * REST controller for saving and retrieving prescriptions.
 */
@RestController
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private Service service;

    /**
     * Save a prescription (doctor token required).
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable String token,
            @RequestBody Prescription prescription
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        return prescriptionService.savePrescription(prescription);
    }

    /**
     * Get prescription(s) for an appointment (doctor token required).
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map<String, Object>> res = prescriptionService.getPrescription(appointmentId);

        // If the service returned prescriptions list, optionally handle empty list here
        Object prescriptionsObj = res.getBody() != null ? res.getBody().get("prescriptions") : null;
        if (prescriptionsObj instanceof List<?> list && list.isEmpty()) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "No prescription exists for that appointment");
            body.put("prescriptions", list);
            return new ResponseEntity<>(body, HttpStatus.OK);
        }

        return res;
    }
}
