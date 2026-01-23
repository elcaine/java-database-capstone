package com.project.back_end.repository;

import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DoctorRepository
 * Provides CRUD operations and custom queries for the Doctor model.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Find a doctor by email address.
     *
     * @param email doctor's email
     * @return Doctor
     */
    Doctor findByEmail(String email);

    /**
     * Find doctors by partial name match.
     */
    @Query("""
        SELECT d
        FROM Doctor d
        WHERE d.name LIKE CONCAT('%', :name, '%')
    """)
    List<Doctor> findByNameLike(@Param("name") String name);

    /**
     * Filter doctors by partial name and exact specialty (case-insensitive).
     */
    @Query("""
        SELECT d
        FROM Doctor d
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))
          AND LOWER(d.specialty) = LOWER(:specialty)
    """)
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
            @Param("name") String name,
            @Param("specialty") String specialty
    );

    /**
     * Find doctors by specialty, ignoring case.
     */
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}