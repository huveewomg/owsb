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