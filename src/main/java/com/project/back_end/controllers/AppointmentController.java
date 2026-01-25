package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "doctor");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(Map.of("message", "Unauthorized"), tokenValidation.getStatusCode());
        }

        LocalDate appointmentDate = LocalDate.parse(date);
        Map<String, Object> res = appointmentService.getAppointment(patientName, appointmentDate, token);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @PathVariable String token,
            @Valid @RequestBody Appointment appointment
    ) {
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return tokenValidation;
        }

        int appointmentValidation = service.validateAppointment(appointment);

        // Per template guidance: invalid doctor ID or unavailable slot should return appropriate failure.
        if (appointmentValidation == -1) {
            return new ResponseEntity<>(Map.of("message", "Doctor not found."), HttpStatus.NOT_FOUND);
        }

        if (appointmentValidation == 0) {
            return new ResponseEntity<>(Map.of("message", "Appointment time unavailable."), HttpStatus.CONFLICT);
        }

        int booked = appointmentService.bookAppointment(appointment);

        Map<String, String> res = new HashMap<>();
        if (booked == 1) {
            res.put("message", "Appointment booked successfully.");
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        }

        res.put("message", "Failed to book appointment.");
        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable String token,
            @Valid @RequestBody Appointment appointment
    ) {
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return tokenValidation;
        }

        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return tokenValidation;
        }

        return appointmentService.cancelAppointment(id, token);
    }
}
