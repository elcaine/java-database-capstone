package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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
     * Compatibility overload (some code calls generateToken(userId, identifier)).
     * Current token design uses the identifier as the JWT subject, so we ignore userId.
     */
    public String generateToken(Long userId, String identifier) {
        return generateToken(identifier);
    }

    /**
     * Compatibility method name used elsewhere in your code.
     * In this design, the token subject is the identifier.
     * For doctor/patient, that identifier is email.
     */
    public String getEmailFromToken(String token) {
        return extractIdentifier(token);
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

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
