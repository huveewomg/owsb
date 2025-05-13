package com.owsb.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PRItem class representing an item in a purchase requisition
 * Demonstrates strong encapsulation and validation
 */
public class PRItem {
    private String itemID;
    private String itemName; // For display purposes
    private int quantity;
    private Date requiredDate;
    private String suggestedSupplierID;
    private double unitPrice; // For cost estimation
    private double estimatedCost; // quantity * unitPrice
    
    /**
     * Constructor for PRItem
     * @param itemID Item ID
     * @param itemName Item name
     * @param quantity Quantity required
     * @param requiredDate Required delivery date
     * @param suggestedSupplierID Suggested supplier ID
     * @param unitPrice Unit price for cost estimation
     */
    public PRItem(String itemID, String itemName, int quantity, Date requiredDate, 
                 String suggestedSupplierID, double unitPrice) {
        this.itemID = itemID;
        this.itemName = itemName;
        setQuantity(quantity); // Use setter for validation
        this.requiredDate = requiredDate;
        this.suggestedSupplierID = suggestedSupplierID;
        this.unitPrice = unitPrice;
        calculateEstimatedCost();
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
        calculateEstimatedCost();
    }
    
    public Date getRequiredDate() {
        return requiredDate;
    }
    
    public void setRequiredDate(Date requiredDate) {
        this.requiredDate = requiredDate;
    }
    
    public String getSuggestedSupplierID() {
        return suggestedSupplierID;
    }
    
    public void setSuggestedSupplierID(String suggestedSupplierID) {
        this.suggestedSupplierID = suggestedSupplierID;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public double getEstimatedCost() {
        return estimatedCost;
    }
    
    /**
     * Calculate the estimated cost
     */
    private void calculateEstimatedCost() {
        this.estimatedCost = this.quantity * this.unitPrice;
    }
    
    /**
     * Get formatted required date string
     * @return Required date in YYYY-MM-DD format
     */
    public String getFormattedRequiredDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(requiredDate);
    }
    
    /**
     * Create a string representation for JSON serialization
     */
    @Override
    public String toString() {
        return "PRItem{" +
                "itemID='" + itemID + '\'' +
                ", quantity=" + quantity +
                ", requiredDate='" + getFormattedRequiredDate() + '\'' +
                ", suggestedSupplierID='" + suggestedSupplierID + '\'' +
                '}';
    }
}