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
}