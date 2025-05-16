package com.owsb.model.procurement;

import com.owsb.util.Constants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PurchaseOrder class representing a purchase order (PO)
 * Demonstrates composition with POItem and encapsulation
 */
public class PurchaseOrder {
    
    private String poID;
    private String prID; // Reference to the Purchase Requisition
    private Date date;
    private Date deliveryDate;
    private String purchaseManagerID;
    private String financeManagerID; // For approval
    private Constants.PurchaseOrderStatus status;
    private String notes;
    private List<POItem> items;
    private double totalValue;
    
    /**
     * Constructor for PurchaseOrder
     * @param poID PO ID
     * @param prID PR ID
     * @param date PO creation date
     * @param deliveryDate Expected delivery date
     * @param purchaseManagerID ID of the purchase manager
     * @param status PO status
     * @param notes Optional notes
     */
    public PurchaseOrder(String poID, String prID, Date date, Date deliveryDate, 
                        String purchaseManagerID, Constants.PurchaseOrderStatus status, String notes) {
        this.poID = poID;
        this.prID = prID;
        this.date = date;
        this.deliveryDate = deliveryDate;
        this.purchaseManagerID = purchaseManagerID;
        this.financeManagerID = null; // Will be set when approved
        this.status = status;
        this.notes = notes;
        this.items = new ArrayList<>();
        this.totalValue = 0.0;
    }
    
    /**
     * Constructor with items
     * @param poID PO ID
     * @param prID PR ID
     * @param date PO creation date
     * @param deliveryDate Expected delivery date
     * @param purchaseManagerID ID of the purchase manager
     * @param status PO status
     * @param notes Optional notes
     * @param items List of PO items
     */
    public PurchaseOrder(String poID, String prID, Date date, Date deliveryDate, 
                        String purchaseManagerID, Constants.PurchaseOrderStatus status, String notes, 
                        List<POItem> items) {
        this(poID, prID, date, deliveryDate, purchaseManagerID, status, notes);
        this.items = items;
        calculateTotalValue();
    }
    
    // Getters and setters
    public String getPoID() {
        return poID;
    }
    
    public String getPrID() {
        return prID;
    }
    
    public Date getDate() {
        return date;
    }
    
    public Date getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public String getPurchaseManagerID() {
        return purchaseManagerID;
    }
    
    public String getFinanceManagerID() {
        return financeManagerID;
    }
    
    public void setFinanceManagerID(String financeManagerID) {
        this.financeManagerID = financeManagerID;
    }
    
    public Constants.PurchaseOrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(Constants.PurchaseOrderStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<POItem> getItems() {
        return items;
    }
    
    public void setItems(List<POItem> items) {
        this.items = items;
        calculateTotalValue();
    }
    
    public double getTotalValue() {
        return totalValue;
    }
    
    /**
     * Add an item to the PO
     * @param item Item to add
     */
    public void addItem(POItem item) {
        items.add(item);
        calculateTotalValue();
    }
    
    /**
     * Remove an item from the PO
     * @param index Index of the item to remove
     */
    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            calculateTotalValue();
        }
    }
    
    /**
     * Get the number of items in the PO
     * @return Number of items
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Calculate the total value of the PO
     */
    private void calculateTotalValue() {
        totalValue = 0.0;
        for (POItem item : items) {
            totalValue += item.getTotalCost();
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
     * Get formatted delivery date string
     * @return Delivery date in YYYY-MM-DD format
     */
    public String getFormattedDeliveryDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(deliveryDate);
    }
    
    /**
     * Create a string representation for JSON serialization
     */
    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "poID='" + poID + '\'' +
                ", prID='" + prID + '\'' +
                ", date='" + getFormattedDate() + '\'' +
                ", purchaseManagerID='" + purchaseManagerID + '\'' +
                ", status=" + status +
                ", notes='" + notes + '\'' +
                ", items=" + items +
                '}';
    }
}