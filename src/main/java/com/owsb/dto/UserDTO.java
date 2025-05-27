package com.owsb.dto;

/**
 * Data Transfer Object for User entities
 * Used for serialization/deserialization to/from JSON
 */
public class UserDTO {
    public String userID;
    public String username;
    public String password;
    public String name;
    public String role;
    public String email;
    public boolean rootAdmin;
}