package com.owsb.model.user;

import com.owsb.model.User;
import com.owsb.util.UserRole;

/**
 * PurchaseManager class representing a purchase manager user
 * Example of inheritance and polymorphism
 */
public class PurchaseManager extends User {
    
    /**
     * Constructor for PurchaseManager
     * @param userId User ID
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email
     */
    public PurchaseManager(String userId, String username, String password, String name, String email) {
        super(userId, username, password, name, UserRole.PURCHASE_MANAGER, email);
    }
    
    /**
     * POLYMORPHISM: Override of hasAccess method
     * Check if Purchase Manager has access to specific functionality
     * @param functionality Functionality to check access for
     * @return true if access allowed, false otherwise
     */
    @Override
    public boolean hasAccess(String functionality) {
        // Purchase Manager specific access control
        return switch(functionality) {
            case "ViewItems", "ViewSuppliers", "ViewPR", 
                 "GeneratePO", "ViewPO" -> true;
            default -> false;
        };
    }
    
    /**
     * PurchaseManager specific method
     * @return Special greeting for purchase managers
     */
    public String getPurchaseGreeting() {
        return "Welcome, Purchase Manager " + getName() + "!";
    }
}
