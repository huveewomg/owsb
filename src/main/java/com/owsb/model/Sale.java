package com.owsb.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Sale class representing a daily item-wise sales entry
 * Demonstrates encapsulation by properly protecting data
 */
public class Sale {
    private String saleID;
    private Date date;
    private String itemID;
    private String itemName; // For display purposes
    private int quantity;
    private double salesAmount;
    private String salesManagerID;
    private String notes;
    
    /**
     * Constructor for Sale
     * @param saleID Sale ID
     * @param date Sale date
     * @param itemID Item ID
     * @param itemName Item name for display
     * @param quantity Quantity sold
     * @param salesAmount Total sales amount
     * @param salesManagerID ID of the sales manager
     * @param notes Optional notes
     */
    public Sale(String saleID, Date date, String itemID, String itemName, 
               int quantity, double salesAmount, String salesManagerID, String notes) {
        this.saleID = saleID;
        this.date = date;
        this.itemID = itemID;
        this.itemName = itemName;
        this.quantity = quantity;
        this.salesAmount = salesAmount;
        this.salesManagerID = salesManagerID;
        this.notes = notes;
    }
    
    // Getters and setters with validation
    public String getSaleID() {
        return saleID;
    }
    
    public Date getDate() {
        return date;
    }
    
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
        // Recalculate sales amount if needed
    }
    
    public double getSalesAmount() {
        return salesAmount;
    }
    
    public String getSalesManagerID() {
        return salesManagerID;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    /**
     * Get formatted date string
     * @return Date in YYYY-MM-DD format
     */
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
    
    /**
     * Create a string representation for JSON serialization
     */
    @Override
    public String toString() {
        return "Sale{" +
                "saleID='" + saleID + '\'' +
                ", date='" + getFormattedDate() + '\'' +
                ", itemID='" + itemID + '\'' +
                ", quantity=" + quantity +
                ", salesAmount=" + salesAmount +
                ", salesManagerID='" + salesManagerID + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}