package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DoctorService
 * Manages operations related to doctors: availability, CRUD, login validation, and filtering.
 */
@org.springframework.stereotype.Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService
    ) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Fetch available slots for a doctor on a given date by removing booked slots.
     */
    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        if (doctorId == null || date == null) return Collections.emptyList();

        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) return Collections.emptyList();

        Doctor doctor = doctorOpt.get();

        // Doctor's baseline availability
        List<String> available = new ArrayList<>();
        if (doctor.getAvailableTimes() != null) {
            available.addAll(doctor.getAvailableTimes());
        }

        // Booked appointments for the day
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Appointment> booked = appointmentRepository.findByDoctor_IdAndAppointmentTimeBetween(
                doctorId, start, end
        );

        Set<String> bookedSlots = new HashSet<>();
        for (Appointment a : booked) {
            if (a.getAppointmentTime() != null) {
                bookedSlots.add(a.getAppointmentTime().toLocalTime().toString());
            }
        }

        // Remove booked slots (format must align with what's stored in doctor.availableTimes)
        available.removeIf(slot -> bookedSlots.contains(slot));

        return available;
    }

    /**
     * Save a new doctor.
     * @return 1 success, -1 already exists, 0 internal error
     */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctor == null || doctor.getEmail() == null) return 0;

            Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
            if (existing != null) return -1;

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Update an existing doctor.
     * @return 1 success, -1 not found, 0 internal error
     */
    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor == null || doctor.getId() == null) return 0;

            Optional<Doctor> existingOpt = doctorRepository.findById(doctor.getId());
            if (existingOpt.isEmpty()) return -1;

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Retrieve all doctors.
     */
    @Transactional
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Delete a doctor and all associated appointments first.
     * @return 1 success, -1 not found, 0 internal error
     */
    @Transactional
    public int deleteDoctor(long id) {
        try {
            Optional<Doctor> doctorOpt = doctorRepository.findById(id);
            if (doctorOpt.isEmpty()) return -1;

            appointmentRepository.deleteAllByDoctor_Id(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Validate doctor's login credentials and return a token if valid.
     */
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> res = new HashMap<>();

        if (login == null || login.getEmail() == null || login.getPassword() == null) {
            res.put("message", "Invalid login request.");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        Doctor doctor = doctorRepository.findByEmail(login.getEmail());
        if (doctor == null) {
            res.put("message", "Doctor not found.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }

        if (!Objects.equals(doctor.getPassword(), login.getPassword())) {
            res.put("message", "Invalid credentials.");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // Assignment expectation: doctor token subject = doctor email
        String token = tokenService.generateToken(doctor.getEmail());

        res.put("token", token);
        res.put("message", "Login successful.");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Find doctors by (partial) name match.
     */
    @Transactional
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> res = new HashMap<>();
        String needle = (name == null) ? "" : name.trim();

        List<Doctor> doctors = doctorRepository.findByNameLike(needle);
        res.put("doctors", doctors);
        return res;
    }

    /**
     * Filter by name + specialty + time.
     */
    @Transactional
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> res = new HashMap<>();

        String n = (name == null) ? "" : name.trim();
        String s = (specialty == null) ? "" : specialty.trim();

        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(n, s);
        doctors = filterDoctorByTime(doctors, amOrPm);

        res.put("doctors", doctors);
        return res;
    }

    /**
     * Filter by name + time.
     */
    @Transactional
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> res = new HashMap<>();

        String n = (name == null) ? "" : name.trim();
        List<Doctor> doctors = doctorRepository.findByNameLike(n);
        doctors = filterDoctorByTime(doctors, amOrPm);

        res.put("doctors", doctors);
        return res;
    }

    /**
     * Filter by name + specialty.
     */
    @Transactional
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        Map<String, Object> res = new HashMap<>();

        String n = (name == null) ? "" : name.trim();
        String s = (specilty == null) ? "" : specilty.trim();

        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(n, s);
        res.put("doctors", doctors);
        return res;
    }

    /**
     * Filter by time + specialty.
     */
    @Transactional
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        Map<String, Object> res = new HashMap<>();

        String s = (specilty == null) ? "" : specilty.trim();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(s);
        doctors = filterDoctorByTime(doctors, amOrPm);

        res.put("doctors", doctors);
        return res;
    }

    /**
     * Filter by specialty.
     */
    @Transactional
    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        Map<String, Object> res = new HashMap<>();

        String s = (specilty == null) ? "" : specilty.trim();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(s);

        res.put("doctors", doctors);
        return res;
    }

    /**
     * Filter all doctors by time (AM/PM).
     */
    @Transactional
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> res = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findAll();
        doctors = filterDoctorByTime(doctors, amOrPm);

        res.put("doctors", doctors);
        return res;
    }

    /**
     * Filters a list of doctors by AM/PM availability.
     * Assumes doctor.getAvailableTimes() returns List<String> slots containing "AM" or "PM".
     */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (doctors == null) return Collections.emptyList();
        if (amOrPm == null || amOrPm.isBlank() || "null".equalsIgnoreCase(amOrPm)) return doctors;

        String target = amOrPm.trim().toUpperCase(Locale.ROOT);

        List<Doctor> filtered = new ArrayList<>();
        for (Doctor d : doctors) {
            List<String> slots = d.getAvailableTimes();
            if (slots == null) continue;

            boolean match = false;
            for (String slot : slots) {
                if (slot != null && slot.toUpperCase(Locale.ROOT).contains(target)) {
                    match = true;
                    break;
                }
            }
            if (match) filtered.add(d);
        }

        return filtered;
    }
}
