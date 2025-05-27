package com.owsb.model.user;

import com.owsb.util.UserRole;

/**
 * InventoryManager class representing an inventory manager user
 * Example of inheritance and polymorphism
 */
public class InventoryManager extends User {
    
    /**
     * Constructor for InventoryManager
     * @param userId User ID
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email
     */
    public InventoryManager(String userId, String username, String password, String name, String email) {
        super(userId, username, password, name, UserRole.INVENTORY_MANAGER, email, false);
    }
    
    /**
     * POLYMORPHISM: Override of hasAccess method
     * Check if Inventory Manager has access to specific functionality
     * @param functionality Functionality to check access for
     * @return true if access allowed, false otherwise
     */
    @Override
    public boolean hasAccess(String functionality) {
        // Inventory Manager specific access control
        return switch(functionality) {
            case "ViewItems", "UpdateStock", "StockReports", 
                 "ViewPO", "TrackLowStock" -> true;
            default -> false;
        };
    }
    
    /**
     * InventoryManager specific method to check if stock is low
     * @param itemCode Item code to check
     * @param currentStock Current stock level
     * @param threshold Low stock threshold
     * @return True if stock is below threshold
     */
    public boolean isLowStock(String itemCode, int currentStock, int threshold) {
        return currentStock < threshold;
    }
    
    /**
     * InventoryManager specific method
     * @return Special greeting for inventory managers
     */
    public String getInventoryGreeting() {
        return "Welcome, Inventory Manager " + getName() + "!";
    }
}