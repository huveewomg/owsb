package com.owsb.model.user;

import com.owsb.util.UserRole;

/**
 * Factory class for creating user objects
 * Implements the Factory design pattern
 */
public class UserFactory {
    
    /**
     * Create a user object based on role
     * @param userId User ID
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param role User role
     * @param email Email
     * @return Appropriate User subclass
     */
    public static User createUser(String userId, String username, String password,
                                  String name, UserRole role, String email) {
        // By default, new users are not root admins.
        return createUser(userId, username, password, name, role, email, false);
    }

    /**
     * Create a user object based on role, explicitly setting rootAdmin status.
     * @param userId User ID
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param role User role
     * @param email Email
     * @param rootAdmin root admin status
     * @return Appropriate User subclass
     */
    public static User createUser(String userId, String username, String password,
                                  String name, UserRole role, String email, boolean rootAdmin) {
        // POLYMORPHISM: Return different User subclass based on role
        return switch (role) {
            case ADMIN -> new Administrator(userId, username, password, name, email, rootAdmin);
            case SALES_MANAGER -> new SalesManager(userId, username, password, name, email);
            case PURCHASE_MANAGER -> new PurchaseManager(userId, username, password, name, email);
            case INVENTORY_MANAGER -> new InventoryManager(userId, username, password, name, email);
            case FINANCE_MANAGER -> new FinanceManager(userId, username, password, name, email);
        };
    }
}
