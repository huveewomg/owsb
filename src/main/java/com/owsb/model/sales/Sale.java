package com.owsb.model.sales;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sale class representing a combined sale with multiple items
 * Demonstrates composition by containing SaleItem objects
 */
public class Sale {
    private String saleID;
    private Date date;
    private String salesManagerID;
    private String notes;
    private List<SaleItem> items;
    private double totalAmount;
    
    /**
     * Constructor for Sale
     * @param saleID Sale ID
     * @param date Sale date
     * @param salesManagerID ID of the sales manager
     * @param notes Optional notes
     */
    public Sale(String saleID, Date date, String salesManagerID, String notes) {
        this.saleID = saleID;
        this.date = date;
        this.salesManagerID = salesManagerID;
        this.notes = notes;
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }
    
    /**
     * Constructor with items
     * @param saleID Sale ID
     * @param date Sale date
     * @param salesManagerID ID of the sales manager
     * @param notes Optional notes
     * @param items List of sale items
     */
    public Sale(String saleID, Date date, String salesManagerID, String notes, List<SaleItem> items) {
        this(saleID, date, salesManagerID, notes);
        this.items = new ArrayList<>(items);
        calculateTotalAmount();
    }
    
    // Getters and setters
    public String getSaleID() {
        return saleID;
    }
    
    public Date getDate() {
        return date;
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
    
    public List<SaleItem> getItems() {
        return items;
    }
    
    public void setItems(List<SaleItem> items) {
        this.items = new ArrayList<>(items);
        calculateTotalAmount();
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    /**
     * Add an item to the sale
     * @param item Item to add
     */
    public void addItem(SaleItem item) {
        items.add(item);
        calculateTotalAmount();
    }
    
    /**
     * Remove an item from the sale
     * @param index Index of the item to remove
     */
    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            calculateTotalAmount();
        }
    }
    
    /**
     * Get the item count
     * @return Number of items in the sale
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Calculate the total sale amount
     */
    private void calculateTotalAmount() {
        totalAmount = 0.0;
        for (SaleItem item : items) {
            totalAmount += item.getSubtotal();
        }
    }
    
    /**
     * Get total cost price (without profit)
     * @return Total cost price
     */
    public double getTotalCostPrice() {
        double totalCost = 0.0;
        for (SaleItem item : items) {
            totalCost += item.getCostPrice();
        }
        return totalCost;
    }
    
    /**
     * Get total profit amount
     * @return Total profit amount
     */
    public double getTotalProfitAmount() {
        return totalAmount - getTotalCostPrice();
    }
    
    /**
     * Get the average profit ratio
     * @return Average profit ratio
     */
    public double getAverageProfitRatio() {
        if (getTotalCostPrice() == 0) {
            return 0;
        }
        return getTotalProfitAmount() / getTotalCostPrice();
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
                ", salesManagerID='" + salesManagerID + '\'' +
                ", notes='" + notes + '\'' +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                '}';
    }
}