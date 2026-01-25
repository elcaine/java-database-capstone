package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AppointmentService
 * Handles operations related to appointments: booking, updating, canceling, retrieving, and status changes.
 *
 * Notes:
 * - Package/name matches assignment scaffold: com.project.back_end.services
 * - Uses constructor injection as required by assignment comments.
 * - Uses @Transactional where the template explicitly calls it out.
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final Service service;                 // shared Service.java (assignment expects this dependency)
    private final TokenService tokenService;       // assignment lists TokenService as a dependency too
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            Service service,
            TokenService tokenService,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Book Appointment Method
     * @return 1 if successful, 0 if an error occurs.
     */
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Update Appointment Method
     * Validates existence + basic correctness + collisions, then saves.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> res = new HashMap<>();

        if (appointment == null || appointment.getId() == null) {
            res.put("message", "Invalid appointment payload.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
        if (existingOpt.isEmpty()) {
            res.put("message", "Appointment not found.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }

        Map<String, String> validationErrors = validateAppointmentForUpdate(appointment);
        if (!validationErrors.isEmpty()) {
            // Coursera patterns typically want "message" key
            return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
        }

        try {
            appointmentRepository.save(appointment);
            res.put("message", "Appointment updated successfully.");
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            res.put("message", "Failed to update appointment.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancel Appointment Method
     * Deletes appointment if it exists and the patient from the token owns it.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> res = new HashMap<>();

        Optional<Appointment> apptOpt = appointmentRepository.findById(id);
        if (apptOpt.isEmpty()) {
            res.put("message", "Appointment not found.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }

        Appointment appt = apptOpt.get();

        // Identify patient from token. TokenService subject = email for patient.
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

        if (appt.getPatient() == null || appt.getPatient().getId() == null) {
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        if (!Objects.equals(appt.getPatient().getId(), patient.getId())) {
            res.put("message", "You are not authorized to cancel this appointment.");
            return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
        }

        try {
            appointmentRepository.delete(appt);
            res.put("message", "Appointment canceled successfully.");
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            res.put("message", "Failed to cancel appointment.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get Appointments Method
     * Retrieves appointments for the doctor (derived from token) on a given date,
     * optionally filtered by patient name.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> res = new HashMap<>();

        // Identify doctor from token. TokenService subject = email for doctor.
        String emailFromToken = tokenService.getEmailFromToken(token);
        if (emailFromToken == null || emailFromToken.isBlank() || date == null) {
            res.put("appointments", Collections.emptyList());
            return res;
        }

        // doctor is located by email
        Doctor doctor = doctorRepository.findByEmail(emailFromToken);
        if (doctor == null || doctor.getId() == null) {
            res.put("appointments", Collections.emptyList());
            return res;
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Appointment> appts = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctor.getId(), start, end
        );

        String filter = (pname == null) ? "" : pname.trim();
        if (!filter.isEmpty() && !"null".equalsIgnoreCase(filter)) {
            String needle = filter.toLowerCase(Locale.ROOT);
            appts = appts.stream()
                    .filter(a -> a.getPatient() != null
                            && a.getPatient().getName() != null
                            && a.getPatient().getName().toLowerCase(Locale.ROOT).contains(needle))
                    .toList();
        }

        res.put("appointments", appts);
        return res;
    }

    /**
     * Change Status Method
     * Updates the status of an appointment in the database.
     * Template explicitly calls for this + @Transactional.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> changeStatus(Long appointmentId, int status) {
        Map<String, String> res = new HashMap<>();

        if (appointmentId == null) {
            res.put("message", "Invalid appointment id.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
        if (apptOpt.isEmpty()) {
            res.put("message", "Appointment not found.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }

        Appointment appt = apptOpt.get();
        appt.setStatus(status);

        try {
            appointmentRepository.save(appt);
            res.put("message", "Appointment status updated.");
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            res.put("message", "Failed to update appointment status.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validation used for update scenarios.
     * Returns empty map if valid; else map with "message".
     *
     * This stays deliberately simple and assignment-friendly.
     */
    private Map<String, String> validateAppointmentForUpdate(Appointment appointment) {
        Map<String, String> errors = new HashMap<>();

        if (appointment.getAppointmentTime() == null) {
            errors.put("message", "Appointment time is required.");
            return errors;
        }

        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
            errors.put("message", "Appointment time cannot be in the past.");
            return errors;
        }

        // doctor check
        Doctor doctor = appointment.getDoctor();
        Long doctorId = (doctor != null) ? doctor.getId() : null;
        if (doctorId == null || doctorRepository.findById(doctorId).isEmpty()) {
            errors.put("message", "Invalid doctor ID.");
            return errors;
        }

        // patient check
        Patient patient = appointment.getPatient();
        Long patientId = (patient != null) ? patient.getId() : null;
        if (patientId == null || patientRepository.findById(patientId).isEmpty()) {
            errors.put("message", "Invalid patient ID.");
            return errors;
        }

        // availability/collision: one-hour slot overlap check
        LocalDateTime start = appointment.getAppointmentTime();
        LocalDateTime end = appointment.getAppointmentTime().plusHours(1);

        List<Appointment> collisions = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, start, end
        );

        // ignore itself during update
        if (appointment.getId() != null) {
            collisions = collisions.stream()
                    .filter(a -> a.getId() != null && !a.getId().equals(appointment.getId()))
                    .toList();
        }

        if (!collisions.isEmpty()) {
            errors.put("message", "Appointment already booked for this time.");
        }

        return errors;
    }
}
