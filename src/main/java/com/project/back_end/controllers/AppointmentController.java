package com.example.cliniccapstone.controller;

import com.example.cliniccapstone.model.Appointment;
import com.example.cliniccapstone.service.AppointmentService;
import com.example.cliniccapstone.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * AppointmentController
 * REST controller for appointment booking, retrieval, updates, and cancellations.
 */
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private Service service;

    /**
     * Get appointments for a doctor by date, patient name, and token.
     */
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token
    ) {
        // Validate doctor token
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        LocalDate parsedDate = LocalDate.parse(date);
        Map<String, Object> result = appointmentService.getAppointment(patientName, parsedDate, token);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Book an appointment (patient token required).
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment
    ) {
        // Validate patient token
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        // Validate appointment availability
        int valid = service.validateAppointment(appointment);
        if (valid == -1) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Doctor not found.");
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
        if (valid == 0) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Appointment time unavailable.");
            return new ResponseEntity<>(err, HttpStatus.CONFLICT);
        }

        int booked = appointmentService.bookAppointment(appointment);
        if (booked == 1) {
            Map<String, String> ok = new HashMap<>();
            ok.put("message", "Appointment booked successfully.");
            return new ResponseEntity<>(ok, HttpStatus.CREATED);
        }

        Map<String, String> err = new HashMap<>();
        err.put("message", "Failed to book appointment.");
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Update an appointment (patient token required).
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        return appointmentService.updateAppointment(appointment);
    }

    /**
     * Cancel an appointment (patient token required).
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        return appointmentService.cancelAppointment(id, token);
    }
}
