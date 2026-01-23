package com.project.back_end.service;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * PatientService
 * Handles patient-related operations including retrieving appointments and patient details,
 * with authorization based on JWT token.
 */
@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    /**
     * Retrieves a patient's appointments, ensuring the patientId matches the token's user.
     * Returns AppointmentDTO list in the response map under key "appointments".
     */
    public ResponseEntity<Map<String, Object>> getPatientAppointments(Long patientId, String token) {
        Map<String, Object> res = new HashMap<>();

        String emailFromToken = tokenService.getEmailFromToken(token);
        if (emailFromToken == null || emailFromToken.isBlank()) {
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        Patient patient = patientRepository.findByEmail(emailFromToken);
        if (patient == null || patient.getId() == null) {
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        if (!Objects.equals(patient.getId(), patientId)) {
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        List<Appointment> appts = appointmentRepository.findByPatientId(patientId);

        List<AppointmentDTO> dtos = new ArrayList<>();
        for (Appointment a : appts) {
            dtos.add(toDTO(a));
        }

        res.put("appointments", dtos);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Filters appointments by condition ("past" or "future") for a specific patient.
     * Uses status: 1 for past, 0 for future.
     */
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> res = new HashMap<>();

        if (condition == null || id == null) {
            res.put("message", "Invalid request.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        int status;
        String c = condition.trim().toLowerCase(Locale.ROOT);
        if ("past".equals(c)) {
            status = 1;
        } else if ("future".equals(c)) {
            status = 0;
        } else {
            res.put("message", "Invalid condition. Use 'past' or 'future'.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        List<Appointment> appts = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);

        List<AppointmentDTO> dtos = new ArrayList<>();
        for (Appointment a : appts) {
            dtos.add(toDTO(a));
        }

        res.put("appointments", dtos);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Filters appointments by doctor's name for a specific patient.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> res = new HashMap<>();

        if (patientId == null) {
            res.put("message", "Invalid patient id.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        String n = (name == null) ? "" : name.trim();

        List<Appointment> appts = appointmentRepository.filterByDoctorNameAndPatientId(n, patientId);

        List<AppointmentDTO> dtos = new ArrayList<>();
        for (Appointment a : appts) {
            dtos.add(toDTO(a));
        }

        res.put("appointments", dtos);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Filters appointments by doctor's name and condition ("past"/"future") for a specific patient.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> res = new HashMap<>();

        String c = (condition == null) ? "" : condition.trim().toLowerCase(Locale.ROOT);
        int status;
        if ("past".equals(c)) status = 1;
        else if ("future".equals(c)) status = 0;
        else {
            res.put("message", "Invalid condition. Use 'past' or 'future'.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        String n = (name == null) ? "" : name.trim();

        List<Appointment> appts = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(n, patientId, status);

        List<AppointmentDTO> dtos = new ArrayList<>();
        for (Appointment a : appts) {
            dtos.add(toDTO(a));
        }

        res.put("appointments", dtos);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Fetch the patient's details using the JWT token (email).
     */
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> res = new HashMap<>();

        String email = tokenService.getEmailFromToken(token);
        if (email == null || email.isBlank()) {
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            res.put("message", "Patient not found.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }

        res.put("patient", patient);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, Object>> createPatient(Patient patient) {
        Map<String, Object> res = new HashMap<>();
    
        if (patient == null) {
            res.put("message", "Invalid patient.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    
        // Basic duplicate check (common expectation)
        if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            Patient existing = patientRepository.findByEmail(patient.getEmail().trim());
            if (existing != null) {
                res.put("message", "Email already in use.");
                return new ResponseEntity<>(res, HttpStatus.CONFLICT);
            }
        }
    
        Patient saved = patientRepository.save(patient);
        res.put("patient", saved);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }
    
    /**
     * Helper: convert Appointment entity to AppointmentDTO.
     * Adjust getters according to your entity fields.
     */
    private AppointmentDTO toDTO(Appointment a) {
        Long doctorId = (a.getDoctor() != null) ? a.getDoctor().getId() : null;
        String doctorName = (a.getDoctor() != null) ? a.getDoctor().getName() : null;

        Long patientId = (a.getPatient() != null) ? a.getPatient().getId() : null;
        String patientName = (a.getPatient() != null) ? a.getPatient().getName() : null;
        String patientEmail = (a.getPatient() != null) ? a.getPatient().getEmail() : null;
        String patientPhone = (a.getPatient() != null) ? a.getPatient().getPhone() : null;
        String patientAddress = (a.getPatient() != null) ? a.getPatient().getAddress() : null;

        return new AppointmentDTO(
                a.getId(),
                doctorId,
                doctorName,
                patientId,
                patientName,
                patientEmail,
                patientPhone,
                patientAddress,
                a.getAppointmentTime(),
                a.getStatus()
        );
    }
}
