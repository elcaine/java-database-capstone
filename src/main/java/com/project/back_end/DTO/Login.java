package com.project.back_end.dto;

/**
 * Login
 * Data Transfer Object for receiving login credentials from clients.
 */
public class Login {

    private String identifier;
    private String password;

    public Login() {
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
