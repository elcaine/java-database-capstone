package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.service.AppointmentService;
import com.project.back_end.service.ClinicService;
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
    private final ClinicService service;

    public AppointmentController(AppointmentService appointmentService, ClinicService service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            // preserve the service's response body as much as possible
            Map<String, Object> err = new HashMap<>();
            err.putAll(tokenRes.getBody() == null ? Map.of("message", "Unauthorized") : tokenRes.getBody());
            return new ResponseEntity<>(err, tokenRes.getStatusCode());
        }

        LocalDate parsedDate = LocalDate.parse(date);
        Map<String, Object> result = appointmentService.getAppointment(patientName, parsedDate, token);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @PathVariable String token,
            @Valid @RequestBody Appointment appointment
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            return tokenRes;
        }

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

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable String token,
            @Valid @RequestBody Appointment appointment
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            return tokenRes;
        }
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            return tokenRes;
        }
        return appointmentService.cancelAppointment(id, token);
    }
}
