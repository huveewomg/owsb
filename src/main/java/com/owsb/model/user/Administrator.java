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
    
}
