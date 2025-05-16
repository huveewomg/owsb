package com.owsb.model.user;

import com.owsb.util.UserRole;

/**
 * Abstract base class for all user types
 * Demonstrates inheritance and polymorphism
 */
public abstract class User {
    // ENCAPSULATION: Private fields
    private String userId;
    private String username;
    private String password;
    private String name;
    private UserRole role;
    private String email;
    
    /**
     * Constructor for User class
     * @param userId Unique identifier for user
     * @param username Username for login
     * @param password Password for login
     * @param name Full name of user
     * @param role Role of user
     * @param email Email of user
     */
    public User(String userId, String username, String password, String name, UserRole role, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.email = email;
    }
    
    // Getters and setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * ABSTRACTION: Abstract method that forces subclasses to implement
     * Check if user has access to specific functionality
     * @param functionality Functionality to check access for
     * @return true if user has access, false otherwise
     */
    public abstract boolean hasAccess(String functionality);
    
    /**
     * Common method implementation for all user types
     * @return String representation of user
     */
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", email='" + email + '\'' +
                '}';
    }
}
