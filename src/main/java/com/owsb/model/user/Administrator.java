package com.owsb.model.user;

import com.owsb.model.User;
import com.owsb.util.UserRole;

/**
 * Administrator class representing an admin user
 * Example of inheritance and polymorphism
 */
public class Administrator extends User {
    
    /**
     * Constructor for Administrator
     * @param userId User ID
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email
     */
    public Administrator(String userId, String username, String password, String name, String email) {
        super(userId, username, password, name, UserRole.ADMIN, email);
    }
    
    /**
     * POLYMORPHISM: Override of hasAccess method
     * Administrators have access to all functionalities
     * @param functionality Functionality to check access for
     * @return true for all functionalities
     */
    @Override
    public boolean hasAccess(String functionality) {
        // Administrators have access to everything
        return true;
    }
    
    /**
     * Admin-specific method for user creation
     * @param user User to create
     * @return Whether creation was successful
     */
    public boolean createUser(User user) {
        // Implementation would save user to the users.txt file
        // For demo purposes, just return true
        return true;
    }
    
    /**
     * Admin-specific method for user deletion
     * @param userId ID of user to delete
     * @return Whether deletion was successful
     */
    public boolean deleteUser(String userId) {
        // Implementation would remove user from the users.txt file
        // For demo purposes, just return true
        return true;
    }
}
