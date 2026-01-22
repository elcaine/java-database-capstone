package com.example.cliniccapstone.controller;

import com.example.cliniccapstone.dto.Login;
import com.example.cliniccapstone.model.Doctor;
import com.example.cliniccapstone.service.DoctorService;
import com.example.cliniccapstone.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DoctorController
 * REST controller for doctor operations: availability, CRUD, login, and filtering.
 */
@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private Service service;

    /**
     * Get doctor availability for a given date.
     * Validates token for the provided user role.
     */
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token
    ) {
        // Validate token for role
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, user);
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        LocalDate parsedDate = LocalDate.parse(date);
        List<String> availability = doctorService.getDoctorAvailability(doctorId, parsedDate);

        Map<String, Object> res = new HashMap<>();
        res.put("availability", availability);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Get all doctors.
     */
    @GetMapping
    public Map<String, Object> getDoctors() {
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", doctorService.getDoctors());
        return res;
    }

    /**
     * Add a new doctor (admin token required).
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "admin");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.saveDoctor(doctor);
        Map<String, String> res = new HashMap<>();

        if (result == 1) {
            res.put("message", "Doctor added to db");
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } else if (result == -1) {
            res.put("message", "Doctor already exists");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        } else {
            res.put("message", "Some internal error occurred");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Doctor login.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    /**
     * Update doctor details (admin token required).
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "admin");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.updateDoctor(doctor);
        Map<String, String> res = new HashMap<>();

        if (result == 1) {
            res.put("message", "Doctor updated");
            return new ResponseEntity<>(res, HttpStatus.OK);
        } else if (result == -1) {
            res.put("message", "Doctor not found");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        } else {
            res.put("message", "Some internal error occurred");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete doctor (admin token required).
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "admin");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Unauthorized");
            return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.deleteDoctor(id);
        Map<String, String> res = new HashMap<>();

        if (result == 1) {
            res.put("message", "Doctor deleted successfully");
            return new ResponseEntity<>(res, HttpStatus.OK);
        } else if (result == -1) {
            res.put("message", "Doctor not found with id");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        } else {
            res.put("message", "Some internal error occurred");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filter doctors by name, time, and specialty.
     */
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public Map<String, Object> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality
    ) {
        return service.filterDoctor(name, speciality, time);
    }
}
