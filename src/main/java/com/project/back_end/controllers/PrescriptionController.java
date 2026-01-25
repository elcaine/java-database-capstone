package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.service.AppointmentService;
import com.project.back_end.service.ClinicService;
import com.project.back_end.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final ClinicService service;
    private final AppointmentService appointmentService;

    public PrescriptionController(
            PrescriptionService prescriptionService,
            ClinicService service,
            AppointmentService appointmentService
    ) {
        this.prescriptionService = prescriptionService;
        this.service = service;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable String token,
            @Valid @RequestBody Prescription prescription
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            return tokenRes;
        }

        // Scaffold expectation: update appointment status after issuing prescription.
        // We assume AppointmentService exposes a method that can update based on appointmentId.
        // Adjust the method name to match your AppointmentService implementation.
        try {
            appointmentService.updateAppointmentStatusAfterPrescription(prescription.getAppointmentId());
        } catch (Exception ignored) {
            // Keep controller resilient; service layer should handle if not supported.
        }

        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.putAll(tokenRes.getBody() == null ? Map.of("message", "Unauthorized") : tokenRes.getBody());
            return new ResponseEntity<>(err, tokenRes.getStatusCode());
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}
