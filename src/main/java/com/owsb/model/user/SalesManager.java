package com.owsb.model.user;

import com.owsb.util.UserRole;

/**
 * SalesManager class representing a sales manager user
 * Example of inheritance and polymorphism
 */
public class SalesManager extends User {
    
    /**
     * Constructor for SalesManager
     * @param userId User ID
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email
     */
    public SalesManager(String userId, String username, String password, String name, String email) {
        super(userId, username, password, name, UserRole.SALES_MANAGER, email, false);
    }
    
    /**
     * POLYMORPHISM: Override of hasAccess method
     * Check if Sales Manager has access to specific functionality
     * @param functionality Functionality to check access for
     * @return true if access allowed, false otherwise
     */
    @Override
    public boolean hasAccess(String functionality) {
        // Sales Manager specific access control
        return switch (functionality) {
            case "ItemEntry", "SupplierEntry", "SalesEntry", 
                 "CreatePR", "ViewPR", "ViewPO" -> true;
            default -> false;
        };
    }
    
    /**
     * SalesManager specific method
     * @return Special greeting for sales managers
     */
    public String getSalesGreeting() {
        return "Welcome, Sales Manager " + getName() + "!";
    }
}