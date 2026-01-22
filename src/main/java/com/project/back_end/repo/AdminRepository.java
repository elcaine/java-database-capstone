package com.example.cliniccapstone.repository;

import com.example.cliniccapstone.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AdminRepository
 * Provides CRUD operations for the Admin model.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Find an admin by username.
     *
     * @param username the admin's username
     * @return Admin
     */
    Admin findByUsername(String username);
}
