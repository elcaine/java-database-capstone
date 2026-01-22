package com.example.cliniccapstone.dto;

/**
 * LoginDTO
 * Data Transfer Object for receiving login credentials from clients.
 */
public class LoginDTO {

    private String identifier;
    private String password;

    public LoginDTO() {
        // default constructor required for deserialization
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
