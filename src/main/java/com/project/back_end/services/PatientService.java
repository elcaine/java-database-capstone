package com.project.back_end.services;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * PatientService
 * Business logic for patients: create patient, fetch appointments, filter appointments, and fetch patient details.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * createPatient
     * Template expectation: returns 1 on success, 0 on failure.
     */
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * getPatientAppointment
     * Returns a map with key "appointments" containing AppointmentDTO objects.
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long patientId, String token) {
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
     * filterByCondition
     * condition: "past" -> status 1, "future" -> status 0
     */
    @Transactional
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
     * filterByDoctor
     * Filters appointments for a patient by doctor name.
     */
    @Transactional
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
     * filterByDoctorAndCondition
     * Filters appointments for a patient by doctor name and condition ("past"/"future").
     */
    @Transactional
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
     * getPatientDetails
     * Uses token -> email -> patient.
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
