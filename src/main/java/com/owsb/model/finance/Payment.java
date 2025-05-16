package com.owsb.model.finance;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Payment class representing a payment for a purchase order
 * Implements encapsulation with private fields and public methods
 */
public class Payment {
    
    public enum PaymentMethod {
        BANK_TRANSFER("Bank Transfer"),
        CHECK("Check"),
        CREDIT_CARD("Credit Card"),
        CASH("Cash");
        
        private final String displayName;
        
        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static PaymentMethod fromString(String method) {
            for (PaymentMethod paymentMethod : PaymentMethod.values()) {
                if (paymentMethod.name().equalsIgnoreCase(method) || 
                    paymentMethod.getDisplayName().equalsIgnoreCase(method)) {
                    return paymentMethod;
                }
            }
            return BANK_TRANSFER; // Default
        }
    }
    
    public enum Status {
        PENDING("Pending"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private String paymentID;
    private Date date;
    private String poID;
    private String supplierID;
    private double amount;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String financeManagerID;
    private Status status;
    private String notes;
    
    /**
     * Constructor for Payment
     * @param paymentID Payment ID
     * @param date Payment date
     * @param poID Purchase order ID
     * @param supplierID Supplier ID
     * @param amount Payment amount
     * @param paymentMethod Payment method
     * @param referenceNumber Reference number
     * @param financeManagerID Finance manager ID
     * @param status Payment status
     * @param notes Payment notes
     */
    public Payment(String paymentID, Date date, String poID, String supplierID, 
                  double amount, PaymentMethod paymentMethod, String referenceNumber, 
                  String financeManagerID, Status status, String notes) {
        this.paymentID = paymentID;
        this.date = date;
        this.poID = poID;
        this.supplierID = supplierID;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
        this.financeManagerID = financeManagerID;
        this.status = status;
        this.notes = notes;
    }
    
    // Getters and setters
    public String getPaymentID() {
        return paymentID;
    }
    
    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public String getPoID() {
        return poID;
    }
    
    public void setPoID(String poID) {
        this.poID = poID;
    }
    
    public String getSupplierID() {
        return supplierID;
    }
    
    public void setSupplierID(String supplierID) {
        this.supplierID = supplierID;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    
    public String getFinanceManagerID() {
        return financeManagerID;
    }
    
    public void setFinanceManagerID(String financeManagerID) {
        this.financeManagerID = financeManagerID;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
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
     * Generate a reference number based on payment method and date
     * @return Reference number
     */
    public static String generateReferenceNumber(PaymentMethod paymentMethod, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(date);
        
        // Use the first two letters of the payment method as a prefix
        String prefix = paymentMethod.name().substring(0, 2);
        
        // Combine with date and a random 3-digit number
        return prefix + dateStr + String.format("%03d", (int)(Math.random() * 1000));
    }
    
    @Override
    public String toString() {
        return "Payment{" +
                "paymentID='" + paymentID + '\'' +
                ", date=" + getFormattedDate() +
                ", poID='" + poID + '\'' +
                ", supplierID='" + supplierID + '\'' +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                '}';
    }
}