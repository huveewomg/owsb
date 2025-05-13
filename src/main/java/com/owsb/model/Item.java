package com.owsb.model;

import java.time.LocalDate;

/**
 * Item class representing a product with inventory information
 * Demonstrates encapsulation by keeping fields private with getters/setters
 */
public class Item {
    // Basic item properties
    private String itemID;
    private String name;
    private String description;
    private double unitPrice;
    private String category;
    private String supplierID;
    private String dateAdded;
    
    // Inventory properties (combined from inventory.txt)
    private int currentStock;
    private int minimumStock;
    private int maximumStock;
    private String lastUpdated;
    
    /**
     * Constructor with basic item information
     */
    public Item(String itemID, String name, String description, double unitPrice, 
                String category, String supplierID) {
        this.itemID = itemID;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.category = category;
        this.supplierID = supplierID;
        
        // Default values
        this.dateAdded = LocalDate.now().toString();
        this.lastUpdated = LocalDate.now().toString();
        this.currentStock = 0;
        this.minimumStock = 10;
        this.maximumStock = 100;
    }
    
    // Getters and setters (encapsulation)
    public String getItemID() { return itemID; }
    public void setItemID(String itemID) { this.itemID = itemID; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSupplierID() { return supplierID; }
    public void setSupplierID(String supplierID) { this.supplierID = supplierID; }

    public String getDateAdded() { return dateAdded; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }

    public int getMaximumStock() { return maximumStock; }
    public void setMaximumStock(int maximumStock) { this.maximumStock = maximumStock; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    
    
    /**
     * Business logic method to check if item needs reordering
     * Demonstrates behavior in objects (not just data)
     */
    public boolean isLowStock() {
        return currentStock <= minimumStock;
    }

    /**
     * Updates stock level and tracks last update date
     * @param quantity Change in quantity (positive for additions, negative for deductions)
     */
    public void updateStock(int quantity) {
        this.currentStock += quantity;
        this.lastUpdated = LocalDate.now().toString();
    }
}