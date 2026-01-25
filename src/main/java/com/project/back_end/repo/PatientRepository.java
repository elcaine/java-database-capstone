package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PatientRepository
 * Provides CRUD operations for the Patient model.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Find a patient by email address.
     *
     * @param email patient's email
     * @return Patient
     */
    Patient findByEmail(String email);

    /**
     * Find a patient by email or phone number.
     *
     * @param email patient's email
     * @param phone patient's phone number
     * @return Patient
     */
    Patient findByEmailOrPhone(String email, String phone);
}