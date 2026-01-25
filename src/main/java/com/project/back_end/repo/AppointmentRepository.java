package com.project.back_end.repo;

import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Appointments for a doctor within a date/time range
    List<Appointment> findByDoctor_IdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    // Same, filtered by patient name (case-insensitive)
    List<Appointment> findByDoctor_IdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            Long doctorId,
            String patientName,
            LocalDateTime start,
            LocalDateTime end
    );

    // Delete all appointments for a doctor
    @Modifying
    @Transactional
    void deleteAllByDoctor_Id(Long doctorId);

    // All appointments for a patient
    List<Appointment> findByPatient_Id(Long patientId);

    // Appointments for a patient by status ordered by time
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    // Filter appointments by doctor name + patient id
    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.patient.id = :patientId
          AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
    """)
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId
    );

    // Filter appointments by doctor name + patient id + status
    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.patient.id = :patientId
          AND a.status = :status
          AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
    """)
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status
    );

    // Update status by appointment id (template mentions this)
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("status") int status, @Param("id") long id);
}
