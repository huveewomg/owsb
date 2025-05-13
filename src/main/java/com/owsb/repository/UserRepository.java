package com.owsb.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.owsb.dto.UserDTO;
import com.owsb.model.User;
import com.owsb.model.UserFactory;
import com.owsb.util.Constants;
import com.owsb.util.UserRole;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Repository pattern for User entities
 * Handles persistence to/from JSON files
 * Demonstrates Single Responsibility Principle by focusing only on data access
 */
public class UserRepository implements Repository<User> {
    private final Gson gson;
    private final String filePath;
    
    /**
     * Constructor initializes Gson serializer and file path
     */
    public UserRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.filePath = Constants.USER_FILE;
    }
    
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        
        try {
            List<UserDTO> userDTOs = readUsersFromFile();
            
            for (UserDTO dto : userDTOs) {
                users.add(convertToUser(dto));
            }
        } catch (IOException e) {
            System.err.println("Error reading users: " + e.getMessage());
        }
        
        return users;
    }
    
    @Override
    public User findById(String id) {
        try {
            List<UserDTO> userDTOs = readUsersFromFile();
            
            for (UserDTO dto : userDTOs) {
                if (dto.userID.equals(id)) {
                    return convertToUser(dto);
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Find a user by username
     * @param username Username to search for
     * @return User if found, null otherwise
     */
    public User findByUsername(String username) {
        try {
            List<UserDTO> userDTOs = readUsersFromFile();
            
            for (UserDTO dto : userDTOs) {
                if (dto.username.equals(username)) {
                    return convertToUser(dto);
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public boolean save(User user) {
        try {
            List<UserDTO> userDTOs = readUsersFromFile();
            
            // Check for duplicate user ID or username
            for (UserDTO dto : userDTOs) {
                if (dto.userID.equals(user.getUserId()) || dto.username.equals(user.getUsername())) {
                    return false; // User already exists
                }
            }
            
            // Add new user
            userDTOs.add(convertToDTO(user));
            
            // Save to file
            writeUsersToFile(userDTOs);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving user: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean update(User user) {
        try {
            List<UserDTO> userDTOs = readUsersFromFile();
            boolean updated = false;
            
            // Check for duplicate username but different user ID
            for (UserDTO dto : userDTOs) {
                if (dto.username.equals(user.getUsername()) && !dto.userID.equals(user.getUserId())) {
                    return false; // Username already taken by another user
                }
            }
            
            // Find and update existing user
            for (int i = 0; i < userDTOs.size(); i++) {
                if (userDTOs.get(i).userID.equals(user.getUserId())) {
                    userDTOs.set(i, convertToDTO(user));
                    updated = true;
                    break;
                }
            }
            
            if (updated) {
                writeUsersToFile(userDTOs);
                return true;
            }
            return false; // User not found
        } catch (IOException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(String id) {
        try {
            List<UserDTO> userDTOs = readUsersFromFile();
            boolean removed = false;
            
            // Find and remove user
            for (int i = 0; i < userDTOs.size(); i++) {
                if (userDTOs.get(i).userID.equals(id)) {
                    userDTOs.remove(i);
                    removed = true;
                    break;
                }
            }
            
            if (removed) {
                writeUsersToFile(userDTOs);
                return true;
            }
            return false; // User not found
        } catch (IOException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update a user's password
     * @param userId User ID
     * @param newPassword New password
     * @return true if successful, false otherwise
     */
    public boolean updatePassword(String userId, String newPassword) {
        try {
            List<UserDTO> userDTOs = readUsersFromFile();
            boolean updated = false;
            
            // Find and update user's password
            for (int i = 0; i < userDTOs.size(); i++) {
                if (userDTOs.get(i).userID.equals(userId)) {
                    userDTOs.get(i).password = newPassword;
                    updated = true;
                    break;
                }
            }
            
            if (updated) {
                writeUsersToFile(userDTOs);
                return true;
            }
            return false; // User not found
        } catch (IOException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate a new unique user ID
     * @return Next available user ID
     */
    public String generateUserId() {
        List<User> users = findAll();
        
        // Find the highest existing ID
        int highestId = 0;
        for (User user : users) {
            String userId = user.getUserId();
            if (userId.startsWith("U")) {
                try {
                    int num = Integer.parseInt(userId.substring(1));
                    if (num > highestId) {
                        highestId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        // Generate next ID
        return String.format("U%03d", highestId + 1);
    }
    
    /**
     * Read users from JSON file
     * @return List of user DTOs
     * @throws IOException if file error occurs
     */
    private List<UserDTO> readUsersFromFile() throws IOException {
        File file = new File(filePath);
        
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        FileReader reader = new FileReader(file);
        Type listType = new TypeToken<ArrayList<UserDTO>>(){}.getType();
        List<UserDTO> userDTOs = gson.fromJson(reader, listType);
        reader.close();
        
        // Handle null case when file is empty or invalid
        if (userDTOs == null) {
            return new ArrayList<>();
        }
        
        return userDTOs;
    }
    
    /**
     * Write users to JSON file
     * @param userDTOs List of user DTOs to write
     * @throws IOException if file error occurs
     */
    private void writeUsersToFile(List<UserDTO> userDTOs) throws IOException {
        File file = new File(filePath);
        
        // Create directory if it doesn't exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        FileWriter writer = new FileWriter(file);
        gson.toJson(userDTOs, writer);
        writer.flush();
        writer.close();
    }
    
    /**
     * Convert DTO to domain entity
     * @param dto Data Transfer Object
     * @return Domain Entity
     */
    private User convertToUser(UserDTO dto) {
        return UserFactory.createUser(
            dto.userID,
            dto.username,
            dto.password,
            dto.name,
            UserRole.valueOf(dto.role),
            dto.email
        );
    }
    
    /**
     * Convert domain entity to DTO
     * @param user Domain Entity
     * @return Data Transfer Object
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.userID = user.getUserId();
        dto.username = user.getUsername();
        dto.password = user.getPassword();
        dto.name = user.getName();
        dto.role = user.getRole().name();
        dto.email = user.getEmail();
        return dto;
    }
}