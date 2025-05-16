package com.owsb.model;

/**
 * SaleItem class representing an individual item in a sale
 * Demonstrates encapsulation with proper data protection
 */
public class SaleItem {
    private String itemID;
    private String itemName;
    private int quantity;
    private double unitPrice;
    private double profitRatio; // As a decimal, e.g. 0.10 for 10%
    private double subtotal;
    
    /**
     * Constructor for SaleItem
     * @param itemID Item ID
     * @param itemName Item name
     * @param quantity Quantity sold
     * @param unitPrice Unit price
     * @param profitRatio Profit ratio as a decimal (e.g., 0.10 for 10%)
     */
    public SaleItem(String itemID, String itemName, int quantity, double unitPrice, double profitRatio) {
        this.itemID = itemID;
        this.itemName = itemName;
        setQuantity(quantity); // Use setter for validation
        this.unitPrice = unitPrice;
        setProfitRatio(profitRatio); // Use setter for validation
        calculateSubtotal();
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
        calculateSubtotal();
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public double getProfitRatio() {
        return profitRatio;
    }
    
    public void setProfitRatio(double profitRatio) {
        if (profitRatio < 0) {
            throw new IllegalArgumentException("Profit ratio cannot be negative");
        }
        this.profitRatio = profitRatio;
        calculateSubtotal();
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    /**
     * Calculate the subtotal price including profit
     */
    private void calculateSubtotal() {
        // Base price + profit
        double basePrice = unitPrice * quantity;
        double profit = basePrice * profitRatio;
        this.subtotal = Math.round((basePrice + profit) * 100.0) / 100.0;
    }
    
    /**
     * Get the cost price (without profit)
     * @return Cost price
     */
    public double getCostPrice() {
        return unitPrice * quantity;
    }
    
    /**
     * Get the profit amount
     * @return Profit amount
     */
    public double getProfitAmount() {
        return subtotal - getCostPrice();
    }
    
    /**
     * Create a string representation for JSON serialization
     */
    @Override
    public String toString() {
        return "SaleItem{" +
                "itemID='" + itemID + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", profitRatio=" + profitRatio +
                ", subtotal=" + subtotal +
                '}';
    }
}