package com.owsb.util;

import java.io.File;

/**
 * Constants class for centralized configuration
 * This helps avoid hardcoding values throughout the application
 */
public class Constants {
    
    // Base directory for data files
    private static final String DATA_DIR = "data";
    
    // File paths for data files
    public static final String USER_FILE = DATA_DIR + File.separator + "users.txt";
    public static final String ITEM_FILE = DATA_DIR + File.separator + "items.txt";
    public static final String INVENTORY_FILE = DATA_DIR + File.separator + "inventory.txt";
    public static final String SUPPLIER_FILE = DATA_DIR + File.separator + "suppliers.txt";
    public static final String SALES_FILE = DATA_DIR + File.separator + "sales.txt";
    public static final String PR_FILE = DATA_DIR + File.separator + "purchase_requisitions.txt";
    public static final String PO_FILE = DATA_DIR + File.separator + "purchase_orders.txt";
    public static final String PAYMENTS_FILE = DATA_DIR + File.separator + "payments.txt";
    public static final String ITEM_CATEGORY_FILE = DATA_DIR + File.separator + "item_categories.txt";
    public static final String MESSAGES_FILE = DATA_DIR + File.separator + "messages.txt";
    
    // Business constants
    public static final double DEFAULT_PROFIT_RATIO = 0.10; // 10%
    public static final int MINIMUM_ITEMS_REQUIRED = 3; // Minimum items required for a purchase requisition
    
    // Ensure data directory exists
    static {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdir();
            if (!created) {
                System.err.println("Failed to create data directory: " + DATA_DIR);
            }
        }
    }
    
    // Private constructor to prevent instantiation
    private Constants() {
        // This class should not be instantiated
    }

    // Purchase Order Status Enum
    public static enum PurchaseOrderStatus {
        PENDING("Pending"),
        APPROVED("Approved"),
        PENDING_ARRIVAL("Pending Arrival"),
        PENDING_PAYMENT("Pending Payment"),
        COMPLETED("Completed"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled");

        private final String displayName;

        PurchaseOrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Purchase Requisition Status Enum
    public static enum PurchaseRequisitionStatus {
        NEW("New"),
        PENDING_APPROVAL("Pending Approval"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        PROCESSED("Processed"),
        COMPLETED("Completed");

        private final String displayName;

        PurchaseRequisitionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}