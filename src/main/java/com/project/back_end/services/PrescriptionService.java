package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Saves a new prescription.
     * Rule: only ONE prescription per appointmentId.
     * - If prescription exists for appointmentId -> 400 Bad Request
     * - Else save -> 201 Created
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> res = new HashMap<>();

        try {
            if (prescription == null || prescription.getAppointmentId() == null) {
                res.put("message", "Invalid prescription payload.");
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            Long appointmentId = prescription.getAppointmentId();

            // Enforce "one prescription per appointment"
            List<Prescription> existing = prescriptionRepository.findByAppointmentId(appointmentId);
            if (existing != null && !existing.isEmpty()) {
                res.put("message", "Prescription already exists for that appointment");
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            prescriptionRepository.save(prescription);

            res.put("message", "Prescription saved");
            return new ResponseEntity<>(res, HttpStatus.CREATED);

        } catch (Exception e) {
            res.put("message", "Internal Server Error");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves prescriptions for a given appointmentId.
     * Always returns key: "prescriptions"
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
