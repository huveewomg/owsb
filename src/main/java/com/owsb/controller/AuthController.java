package com.owsb.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.owsb.model.User;
import com.owsb.model.UserFactory;
import com.owsb.util.Constants;
import com.owsb.util.FileUtils;
import com.owsb.util.UserRole;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for authentication and user management
 */
public class AuthController {
    // Using constant from Constants class instead of hardcoding
    private User currentUser = null;
    private final Gson gson;
    
    /**
     * Constructor - initializes the Gson parser
     */
    public AuthController() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    /**
     * Authenticate user with username and password
     * @param username Username to authenticate
     * @param password Password to authenticate
     * @return true if authentication successful, false otherwise
     */
    public boolean login(String username, String password) {
        try {
            // Read all users from file
            List<User> users = getAllUsers();
            
            // Find user with matching username and password
            for (User user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    this.currentUser = user;
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user file: " + e.getMessage());
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
     * Get all users from the JSON file
     * @return List of all users
     * @throws IOException if there's an error reading the file
     */
    public List<User> getAllUsers() throws IOException {
        List<User> users = new ArrayList<>();
        
        // Define the type for JSON deserialization
        Type userListType = new TypeToken<ArrayList<UserDTO>>(){}.getType();
        
        // Using FileUtils to read the file
        List<UserDTO> userDTOs = FileUtils.readListFromJson(Constants.USER_FILE, userListType);
        
        // Convert DTOs to domain User objects
        for (UserDTO dto : userDTOs) {
            User user = UserFactory.createUser(
                dto.userID,
                dto.username,
                dto.password,
                dto.name,
                UserRole.valueOf(dto.role),
                dto.email
            );
            users.add(user);
        }
        
        return users;
    }
    
    /**
     * Save a user to the file
     * @param user User to save
     * @return true if successful, false otherwise
     */
    public boolean saveUser(User user) {
        try {
            // Get all existing users
            List<User> users = getAllUsers();
            
            // Convert to DTOs
            List<UserDTO> userDTOs = new ArrayList<>();
            for (User existingUser : users) {
                userDTOs.add(userToDTO(existingUser));
            }
            
            // Add new user if it doesn't exist
            boolean userExists = false;
            for (int i = 0; i < userDTOs.size(); i++) {
                if (userDTOs.get(i).userID.equals(user.getUserId())) {
                    // Update existing user
                    userDTOs.set(i, userToDTO(user));
                    userExists = true;
                    break;
                }
            }
            
            if (!userExists) {
                userDTOs.add(userToDTO(user));
            }
            
            // Save to file
            FileUtils.writeListToJson(Constants.USER_FILE, userDTOs);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving user: " + e.getMessage());
            return false;
        }
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
        try {
            // Validate input
            if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                name == null || name.trim().isEmpty()) {
                return false;
            }
            
            // Check if username already exists
            List<User> existingUsers = getAllUsers();
            for (User user : existingUsers) {
                if (user.getUsername().equals(username)) {
                    return false; // Username already exists
                }
            }
            
            // Generate a unique user ID
            String userId = generateUserId();
            
            // Create new user through factory
            User newUser = UserFactory.createUser(userId, username, password, name, role, email);
            
            // Save user
            return saveUser(newUser);
        } catch (IOException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
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
        try {
            // Get all users
            List<User> users = getAllUsers();
            
            // Check if username already exists (for a different user)
            for (User user : users) {
                if (user.getUsername().equals(username) && !user.getUserId().equals(userId)) {
                    return false; // Username already exists for a different user
                }
            }
            
            // Find and update user
            for (User user : users) {
                if (user.getUserId().equals(userId)) {
                    user.setUsername(username);
                    user.setName(name);
                    user.setEmail(email);
                    
                    // Only update role if different (would require creating a new user object)
                    if (user.getRole() != role) {
                        // Get current password
                        String password = user.getPassword();
                        
                        // Create new user with updated role
                        User updatedUser = UserFactory.createUser(userId, username, password, name, role, email);
                        
                        // Remove old user
                        users.remove(user);
                        
                        // Add updated user
                        users.add(updatedUser);
                    }
                    
                    // Save all users
                    saveUsers(users);
                    return true;
                }
            }
            
            return false; // User not found
        } catch (IOException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user password
     * @param userId User ID
     * @param newPassword New password
     * @return true if update successful, false otherwise
     */
    public boolean updatePassword(String userId, String newPassword) {
        try {
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return false;
            }
            
            List<User> users = getAllUsers();
            
            for (User user : users) {
                if (user.getUserId().equals(userId)) {
                    user.setPassword(newPassword);
                    saveUsers(users);
                    return true;
                }
            }
            
            return false; // User not found
        } catch (IOException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a user
     * @param userId User ID to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteUser(String userId) {
        try {
            List<User> users = getAllUsers();
            
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserId().equals(userId)) {
                    users.remove(i);
                    saveUsers(users);
                    return true;
                }
            }
            
            return false; // User not found
        } catch (IOException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Save all users to file
     * @param users List of users to save
     * @return true if save successful, false otherwise
     */
    private boolean saveUsers(List<User> users) throws IOException {
        List<UserDTO> userDTOs = new ArrayList<>();
        
        for (User user : users) {
            userDTOs.add(userToDTO(user));
        }
        
        FileUtils.writeListToJson(Constants.USER_FILE, userDTOs);
        return true;
    }

        
    /**
     * Generate a unique user ID
     * @return New user ID
     */
    private String generateUserId() throws IOException {
        List<User> users = getAllUsers();
        
        // Find the highest user ID number
        int highestId = 0;
        for (User user : users) {
            String userId = user.getUserId();
            if (userId.startsWith("U")) {
                try {
                    int idNum = Integer.parseInt(userId.substring(1));
                    if (idNum > highestId) {
                        highestId = idNum;
                    }
                } catch (NumberFormatException e) {
                    // Ignore non-numeric IDs
                }
            }
        }
        
        // Create a new ID that's one higher
        return String.format("U%03d", highestId + 1);
    }
    
    /**
     * Convert User to UserDTO
     * @param user User to convert
     * @return UserDTO
     */
    private UserDTO userToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.userID = user.getUserId();
        dto.username = user.getUsername();
        dto.password = user.getPassword();
        dto.name = user.getName();
        dto.role = user.getRole().name();
        dto.email = user.getEmail();
        return dto;
    }
    
    /**
     * Data Transfer Object for User JSON parsing
     * This inner class maps directly to the JSON structure
     */
    private static class UserDTO {
        String userID;
        String username;
        String password;
        String name;
        String role;
        String email;
    }
}