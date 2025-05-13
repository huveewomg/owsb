package com.owsb.model;

/**
 * POItem class representing an item in a purchase order
 * Demonstrates strong encapsulation and validation
 */
public class POItem {
    private String itemID;
    private String itemName; // For display purposes
    private int quantity;
    private String supplierID;
    private String supplierName; // For display purposes
    private double unitPrice;
    private double totalCost; // quantity * unitPrice
    
    /**
     * Constructor for POItem
     * @param itemID Item ID
     * @param itemName Item name
     * @param quantity Quantity ordered
     * @param supplierID Supplier ID
     * @param supplierName Supplier name
     * @param unitPrice Unit price
     */
    public POItem(String itemID, String itemName, int quantity, 
                 String supplierID, String supplierName, double unitPrice) {
        this.itemID = itemID;
        this.itemName = itemName;
        setQuantity(quantity); // Use setter for validation
        this.supplierID = supplierID;
        this.supplierName = supplierName;
        this.unitPrice = unitPrice;
        calculateTotalCost();
    }
    
    // Getters and setters with validation
    public String getItemID() {
        return itemID;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
        calculateTotalCost();
    }
    
    public String getSupplierID() {
        return supplierID;
    }
    
    public void setSupplierID(String supplierID) {
        this.supplierID = supplierID;
    }
    
    public String getSupplierName() {
        return supplierName;
    }
    
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalCost();
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    /**
     * Calculate the total cost
     */
    private void calculateTotalCost() {
        this.totalCost = this.quantity * this.unitPrice;
    }
    
    /**
     * Create a string representation for JSON serialization
     */
    @Override
    public String toString() {
        return "POItem{" +
                "itemID='" + itemID + '\'' +
                ", quantity=" + quantity +
                ", supplierID='" + supplierID + '\'' +
                ", unitPrice=" + unitPrice +
                ", totalCost=" + totalCost +
                '}';
    }
    
    /**
     * Create a POItem from a PRItem
     * @param prItem PRItem to convert from
     * @param supplierName Supplier name
     * @return New POItem
     */
    public static POItem fromPRItem(PRItem prItem, String supplierName) {
        return new POItem(
            prItem.getItemID(),
            prItem.getItemName(),
            prItem.getQuantity(),
            prItem.getSuggestedSupplierID(),
            supplierName,
            prItem.getUnitPrice()
        );
    }
}