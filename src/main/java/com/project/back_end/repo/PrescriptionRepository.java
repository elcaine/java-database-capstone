package com.example.cliniccapstone.repository;

import com.example.cliniccapstone.model.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PrescriptionRepository
 * Provides CRUD operations for Prescription documents in MongoDB.
 */
@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    /**
     * Find prescriptions by appointment ID.
     *
     * @param appointmentId appointment identifier
     * @return list of prescriptions
     */
    List<Prescription> findByAppointmentId(Long appointmentId);
}
