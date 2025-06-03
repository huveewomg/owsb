package com.owsb.controller;

import com.owsb.model.user.User;
import com.owsb.model.user.UserFactory;
import com.owsb.repository.UserRepository;
import com.owsb.util.UserRole;

import java.util.List;

/**
 * Controller for authentication and user management
 */
public class AuthController {
    private User currentUser = null;
    private final UserRepository userRepository;
    
    /**
     * Constructor - initializes the repository
     */
    public AuthController() {
        this.userRepository = new UserRepository();
    }
    
    /**
     * Authenticate user with username and password
     * @param username Username to authenticate
     * @param password Password to authenticate
     * @return true if authentication successful, false otherwise
     */
    public boolean login(String username, String password) {
        // Find user with matching username and password
        User user = userRepository.findByUsername(username);
        
        if (user != null && user.getPassword().equals(password)) {
            this.currentUser = user;
            return true;
        }
        
        return false;
    }
    
    /**
     * Log out current user
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * Get current logged in user
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if current user has access to specific functionality
     * @param functionality Functionality to check access for
     * @return true if user has access, false otherwise
     */
    public boolean hasAccess(String functionality) {
        return isLoggedIn() && currentUser.hasAccess(functionality);
    }
    
    /**
     * Get all users from the repository
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Register a new user
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email
     * @param role User role
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String name, String email, UserRole role) {
        // Validate input
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Check if username already exists
        if (userRepository.findByUsername(username) != null) {
            return false; // Username already exists
        }
        
        // Generate a unique user ID
        String userId = userRepository.generateUserId();

        User newUser = UserFactory.createUser(userId, username, password, name, role, email, false);

        // Save user to repository
        return userRepository.save(newUser);
    }
    
    /**
     * Update an existing user
     * @param userId User ID to update
     * @param username New username
     * @param name New name
     * @param email New email
     * @param role New role
     * @return true if update successful, false otherwise
     */
    public boolean updateUser(String userId, String username, String name, String email, UserRole role) {
        // Get existing user
        User user = userRepository.findById(userId);
        if (user == null) {
            return false; // User not found
        }
        
        // Update user fields
        user.setUsername(username);
        user.setName(name);
        user.setEmail(email);
        
        // If role is different, we need to create a new user with the new role
        if (user.getRole() != role) {
            // Create new user with updated role
            User updatedUser = UserFactory.createUser(
                userId,
                username,
                user.getPassword(),
                name,
                role,
                email,
                user.isRootAdmin()
            );
            
            // Update in repository
            return userRepository.update(updatedUser);
        }
        
        // Update in repository
        return userRepository.update(user);
    }

    /**
     * Update user password
     * @param userId User ID
     * @param newPassword New password
     * @return true if update successful, false otherwise
     */
    public boolean updatePassword(String userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        
        return userRepository.updatePassword(userId, newPassword);
    }

    /**
     * Delete a user
     * @param userId User ID to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteUser(String userId) {
        return userRepository.delete(userId);
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User or null if not found
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId);
    }
}