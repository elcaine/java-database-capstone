package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.service.ClinicService;
import com.project.back_end.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final ClinicService service;

    public DoctorController(DoctorService doctorService, ClinicService service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, user);
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> err = new HashMap<>();
            err.putAll(tokenRes.getBody() == null ? Map.of("message", "Unauthorized") : tokenRes.getBody());
            return new ResponseEntity<>(err, tokenRes.getStatusCode());
        }

        LocalDate parsedDate = LocalDate.parse(date);
        List<String> availability = doctorService.getDoctorAvailability(doctorId, parsedDate);

        Map<String, Object> res = new HashMap<>();
        res.put("availability", availability);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping
    public Map<String, Object> getDoctors() {
        Map<String, Object> res = new HashMap<>();
        res.put("doctors", doctorService.getDoctors());
        return res;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "admin");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            return tokenRes;
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

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@Valid @RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "admin");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            return tokenRes;
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

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenRes = service.validateToken(token, "admin");
        if (!tokenRes.getStatusCode().is2xxSuccessful()) {
            return tokenRes;
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

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public Map<String, Object> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality
    ) {
        return service.filterDoctor(name, speciality, time);
    }
}
