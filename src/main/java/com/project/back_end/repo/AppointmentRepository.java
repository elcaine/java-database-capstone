package com.project.back_end.repository;

import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AppointmentRepository
 * Provides CRUD operations and custom queries for Appointment.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Retrieve appointments for a doctor within a time range, including doctor and availability info.
     */
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN a.doctor d
        WHERE d.id = :doctorId
          AND a.appointmentTime BETWEEN :start AND :end
    """)
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Filter appointments by doctor ID, partial patient name (case-insensitive), and time range.
     * Includes patient and doctor details.
     */
    @Query("""
        SELECT a
        FROM Appointment a
        LEFT JOIN FETCH a.doctor d
        LEFT JOIN FETCH a.patient p
        WHERE d.id = :doctorId
          AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%'))
          AND a.appointmentTime BETWEEN :start AND :end
    """)
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("patientName") String patientName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Delete all appointments for a specific doctor.
     */
    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);

    /**
     * Find all appointments for a specific patient.
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Retrieve appointments for a patient by status ordered by appointment time.
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    /**
     * Search appointments by partial doctor name and patient ID (case-insensitive).
     */
    @Query("""
        SELECT a
        FROM Appointment a
        LEFT JOIN FETCH a.doctor d
        WHERE a.patient.id = :patientId
          AND LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
    """)
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId
    );

    /**
     * Filter appointments by partial doctor name, patient ID, and status (case-insensitive).
     */
    @Query("""
        SELECT a
        FROM Appointment a
        LEFT JOIN FETCH a.doctor d
        WHERE a.patient.id = :patientId
          AND a.status = :status
          AND LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
    """)
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status
    );
}
