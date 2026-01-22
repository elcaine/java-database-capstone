package com.example.cliniccapstone.service;

import com.example.cliniccapstone.dto.Login;
import com.example.cliniccapstone.model.Admin;
import com.example.cliniccapstone.model.Appointment;
import com.example.cliniccapstone.model.Doctor;
import com.example.cliniccapstone.model.Patient;
import com.example.cliniccapstone.repository.AdminRepository;
import com.example.cliniccapstone.repository.DoctorRepository;
import com.example.cliniccapstone.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Service
 * Central service for authentication, doctor/patient management, and appointment validation.
 */
@Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(
            TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService
    ) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * Validate a token for a given user role.
     * Returns 401 Unauthorized if invalid/expired; otherwise returns 200 OK with empty body map.
     */
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> res = new HashMap<>();

        boolean valid = tokenService.validateToken(token, user);
        if (!valid) {
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Validate admin credentials and return token if authenticated.
     */
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> res = new HashMap<>();

        if (receivedAdmin == null || receivedAdmin.getUsername() == null || receivedAdmin.getPassword() == null) {
            res.put("message", "Invalid login request.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
        if (admin == null) {
            res.put("message", "Invalid credentials.");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        if (!Objects.equals(admin.getPassword(), receivedAdmin.getPassword())) {
            res.put("message", "Invalid credentials.");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(admin.getId(), "admin");
        res.put("token", token);
        res.put("message", "Login successful.");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Filter doctors based on name, specialty, and available time.
     * Returns a map with key "doctors".
     */
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        String n = (name == null) ? "" : name.trim();
        String s = (specialty == null) ? "" : specialty.trim();
        String t = (time == null) ? "" : time.trim();

        // Common Coursera pattern: "null" is passed as a literal string from frontend
        boolean hasName = !n.isBlank() && !"null".equalsIgnoreCase(n);
        boolean hasSpec = !s.isBlank() && !"null".equalsIgnoreCase(s);
        boolean hasTime = !t.isBlank() && !"null".equalsIgnoreCase(t);

        if (hasName && hasSpec && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(n, s, t);
        } else if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(n, t);
        } else if (hasName && hasSpec) {
            return doctorService.filterDoctorByNameAndSpecility(n, s);
        } else if (hasSpec && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(s, t);
        } else if (hasSpec) {
            return doctorService.filterDoctorBySpecility(s);
        } else if (hasTime) {
            return doctorService.filterDoctorsByTime(t);
        } else if (hasName) {
            return doctorService.findDoctorByName(n);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("doctors", doctorService.getDoctors());
        return res;
    }

    /**
     * Validate whether an appointment time is available for a doctor.
     * @return 1 if valid, 0 if unavailable, -1 if doctor not found
     */
    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getDoctor().getId() == null
                || appointment.getAppointmentTime() == null) {
            return 0;
        }

        Long doctorId = appointment.getDoctor().getId();
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return -1;
        }

        LocalDate date = appointment.getAppointmentTime().toLocalDate();
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);

        String apptSlot = appointment.getAppointmentTime().toLocalTime().toString();

        // If your availability strings include AM/PM formatting, normalize here accordingly.
        return availableSlots.contains(apptSlot) ? 1 : 0;
    }

    /**
     * Validate that a patient does NOT already exist by email or phone.
     * @return true if patient does not exist, false if exists already
     */
    public boolean validatePatient(Patient patient) {
        if (patient == null) return false;

        String email = patient.getEmail();
        String phone = patient.getPhone();

        Patient existing = patientRepository.findByEmailOrPhone(email, phone);
        return existing == null;
    }

    /**
     * Validate patient login credentials and return token if authenticated.
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> res = new HashMap<>();

        if (login == null || login.getIdentifier() == null || login.getPassword() == null) {
            res.put("message", "Invalid login request.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        Patient patient = patientRepository.findByEmail(login.getIdentifier());
        if (patient == null) {
            res.put("message", "Invalid credentials.");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        if (!Objects.equals(patient.getPassword(), login.getPassword())) {
            res.put("message", "Invalid credentials.");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(patient.getId(), "patient");
        res.put("token", token);
        res.put("message", "Login successful.");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Filter patient appointments by condition and/or doctor name based on the token-identified patient.
     */
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        // Identify patient via token. Method name may differ in your TokenService.
        String email = tokenService.getEmailFromToken(token);

        if (email == null || email.isBlank()) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        Patient patient = patientRepository.findByEmail(email);
        if (patient == null || patient.getId() == null) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        String c = (condition == null) ? "" : condition.trim();
        String n = (name == null) ? "" : name.trim();

        boolean hasCond = !c.isBlank() && !"null".equalsIgnoreCase(c);
        boolean hasName = !n.isBlank() && !"null".equalsIgnoreCase(n);

        if (hasCond && hasName) {
            return patientService.filterByDoctorAndCondition(c, n, patient.getId());
        } else if (hasCond) {
            return patientService.filterByCondition(c, patient.getId());
        } else if (hasName) {
            return patientService.filterByDoctor(n, patient.getId());
        }

        // If no filters, return all appointments with authorization check
        return patientService.getPatientAppointments(patient.getId(), token);
    }
}
