package com.owsb.model;

import com.owsb.util.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PurchaseRequisition class representing a purchase requisition (PR)
 * Demonstrates composition with PRItem and encapsulation
 */
public class PurchaseRequisition {
    
    private String prID;
    private Date date;
    private Date requiredDate;
    private String salesManagerID;
    private Constants.PurchaseRequisitionStatus status;
    private String notes;
    private List<PRItem> items;
    private double estimatedTotal;
    
    /**
     * Constructor for PurchaseRequisition
     * @param prID PR ID
     * @param date PR creation date
     * @param requiredDate Required delivery date
     * @param salesManagerID ID of the sales manager
     * @param status PR status
     * @param notes Optional notes
     */
    public PurchaseRequisition(String prID, Date date, Date requiredDate, String salesManagerID, 
                              Constants.PurchaseRequisitionStatus status, String notes) {
        this.prID = prID;
        this.date = date;
        this.requiredDate = requiredDate;
        this.salesManagerID = salesManagerID;
        this.status = status;
        this.notes = notes;
        this.items = new ArrayList<>();
        this.estimatedTotal = 0.0;
    }
    
    /**
     * Constructor with items
     * @param prID PR ID
     * @param date PR creation date
     * @param requiredDate Required delivery date
     * @param salesManagerID ID of the sales manager
     * @param status PR status
     * @param notes Optional notes
     * @param items List of PR items
     */
    public PurchaseRequisition(String prID, Date date, Date requiredDate, String salesManagerID, 
                              Constants.PurchaseRequisitionStatus status, String notes, List<PRItem> items) {
        this(prID, date, requiredDate, salesManagerID, status, notes);
        this.items = items;
        calculateEstimatedTotal();
    }
    
    // Getters and setters
    public String getPrID() {
        return prID;
    }
    
    public Date getDate() {
        return date;
    }
    
    public Date getRequiredDate() {
        return requiredDate;
    }
    
    public void setRequiredDate(Date requiredDate) {
        this.requiredDate = requiredDate;
    }
    
    public String getSalesManagerID() {
        return salesManagerID;
    }
    
    public Constants.PurchaseRequisitionStatus getStatus() {
        return status;
    }
    
    public void setStatus(Constants.PurchaseRequisitionStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<PRItem> getItems() {
        return items;
    }
    
    public void setItems(List<PRItem> items) {
        this.items = items;
        calculateEstimatedTotal();
    }
    
    public double getEstimatedTotal() {
        return estimatedTotal;
    }
    
    /**
     * Add an item to the PR
     * @param item Item to add
     */
    public void addItem(PRItem item) {
        items.add(item);
        calculateEstimatedTotal();
    }
    
    /**
     * Remove an item from the PR
     * @param index Index of the item to remove
     */
    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            calculateEstimatedTotal();
        }
    }
    
    /**
     * Get the number of items in the PR
     * @return Number of items
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Check if the PR meets the minimum item requirement
     * @param minItems Minimum number of items required
     * @return true if PR meets the requirement
     */
    public boolean meetsMinimumItemRequirement(int minItems) {
        return items.size() >= minItems;
    }
    
    /**
     * Calculate the estimated total value of the PR
     */
    private void calculateEstimatedTotal() {
        estimatedTotal = 0.0;
        for (PRItem item : items) {
            estimatedTotal += item.getEstimatedCost();
        }
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
        return "PurchaseRequisition{" +
                "prID='" + prID + '\'' +
                ", date='" + getFormattedDate() + '\'' +
                ", salesManagerID='" + salesManagerID + '\'' +
                ", status=" + status +
                ", notes='" + notes + '\'' +
                ", items=" + items +
                '}';
    }
}