package com.owsb.util;

/**
 * Enumeration of user roles in the system
 */
public enum UserRole {
    ADMIN("Administrator"),
    SALES_MANAGER("Sales Manager"),
    PURCHASE_MANAGER("Purchase Manager"),
    INVENTORY_MANAGER("Inventory Manager"),
    FINANCE_MANAGER("Finance Manager");
    
    private final String displayName;
    
    /**
     * Constructor
     * @param displayName Human-readable name of the role
     */
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get display name of role
     * @return Human-readable role name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Convert string representation to UserRole enum
     * @param role String representation of role
     * @return UserRole enum value
     * @throws IllegalArgumentException if role is not valid
     */
    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Invalid user role: " + role);
    }
}
