package com.owsb.model.user;

import com.owsb.util.UserRole;

/**
 * FinanceManager class representing a finance manager user
 * Example of inheritance and polymorphism
 */
public class FinanceManager extends User {
    
    /**
     * Constructor for FinanceManager
     * @param userId User ID
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email
     */
    public FinanceManager(String userId, String username, String password, String name, String email) {
        super(userId, username, password, name, UserRole.FINANCE_MANAGER, email, false);
    }
    
    /**
     * POLYMORPHISM: Override of hasAccess method
     * Check if Finance Manager has access to specific functionality
     * @param functionality Functionality to check access for
     * @return true if access allowed, false otherwise
     */
    @Override
    public boolean hasAccess(String functionality) {
        // Finance Manager specific access control
        return switch(functionality) {
            case "ApprovePO", "ViewPR", "ViewPO", "ProcessPayment", 
                 "FinancialReports", "VerifyInventory" -> true;
            default -> false;
        };
    }
    
    /**
     * Finance Manager specific method to approve a purchase order
     * @param poId Purchase Order ID to approve
     * @return Whether approval was successful
     */
    public boolean approvePurchaseOrder(String poId) {
        // Implementation would update the PO status in the database
        // For demo purposes, just return true
        return true;
    }
    
    /**
     * Finance Manager specific method to process payment
     * @param poId Purchase Order ID to process payment for
     * @param amount Payment amount
     * @return Whether payment processing was successful
     */
    public boolean processPayment(String poId, double amount) {
        // Implementation would create a payment record in the database
        // For demo purposes, just return true
        return true;
    }
    
    /**
     * FinanceManager specific method
     * @return Special greeting for finance managers
     */
    public String getFinanceGreeting() {
        return "Welcome, Finance Manager " + getName() + "!";
    }
}