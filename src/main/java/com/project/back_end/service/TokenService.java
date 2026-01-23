package com.project.back_end.service;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AdminRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * TokenService
 * Generates, extracts, and validates JWT tokens.
 */
@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Generates a JWT token using the provided identifier as the subject.
     * identifier = username for Admin; email for Doctor/Patient.
     * Expires in 7 days.
     */
    public String generateToken(String identifier) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 7L * 24 * 60 * 60 * 1000); // 7 days

        return Jwts.builder()
                .subject(identifier)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

        /**
     * Compatibility overload:
     * Some services/controllers call generateToken(userId, identifier).
     * Current token design uses the identifier as the JWT subject, so we ignore userId.
     */
    public String generateToken(Long userId, String identifier) {
        return generateToken(identifier);
    }

    /**
     * Compatibility method:
     * Existing code expects getEmailFromToken(token).
     * In this implementation, the token subject is the identifier (email for doctor/patient).
     */
    public String getEmailFromToken(String token) {
        return extractIdentifier(token);
    }

    /**
     * Compatibility method:
     * Existing code expects getUserIdFromToken(token).
     * We map the token subject back to a user record and return its id.
     */
    public Long getUserIdFromToken(String token) {
        String identifier = extractIdentifier(token);
        if (identifier == null) return null;

        // Admin token subject is username; Doctor/Patient token subject is email.
        Admin admin = adminRepository.findByUsername(identifier);
        if (admin != null) return admin.getId();

        Doctor doctor = doctorRepository.findByEmail(identifier);
        if (doctor != null) return doctor.getId();

        Patient patient = patientRepository.findByEmail(identifier);
        if (patient != null) return patient.getId();

        return null;
    }


    /**
     * Extracts the identifier (subject) from a JWT token.
     */
    public String extractIdentifier(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates whether the token is valid for a given user type (admin/doctor/patient).
     * Returns false if token is invalid/expired or if user does not exist.
     */
    public boolean validateToken(String token, String user) {
        String identifier = extractIdentifier(token);
        if (identifier == null || user == null) return false;

        try {
            // parse again to enforce expiration validation
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            String u = user.trim().toLowerCase();

            if ("admin".equals(u)) {
                Admin admin = adminRepository.findByUsername(identifier);
                return admin != null;
            }

            if ("doctor".equals(u)) {
                Doctor doctor = doctorRepository.findByEmail(identifier);
                return doctor != null;
            }

            if ("patient".equals(u)) {
                Patient patient = patientRepository.findByEmail(identifier);
                return patient != null;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the signing key derived from the configured secret.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
