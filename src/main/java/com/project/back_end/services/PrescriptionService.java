package com.example.cliniccapstone.service;

import com.example.cliniccapstone.model.Prescription;
import com.example.cliniccapstone.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PrescriptionService
 * Handles operations related to prescriptions stored in MongoDB.
 */
@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    /**
     * Saves a prescription to MongoDB.
     * Returns 201 Created on success.
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> res = new HashMap<>();

        try {
            prescriptionRepository.save(prescription);
            res.put("message", "Prescription saved");
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (Exception e) {
            res.put("message", "Internal Server Error");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves prescription(s) for a given appointment ID.
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> res = new HashMap<>();

        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            res.put("prescriptions", prescriptions);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            res.put("message", "Internal Server Error");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
