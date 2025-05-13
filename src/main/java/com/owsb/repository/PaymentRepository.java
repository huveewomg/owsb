package com.owsb.repository;

import com.owsb.model.Payment;
import com.owsb.util.Constants;
import com.owsb.util.FileUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for Payment entities
 * Implements the Repository interface demonstrating polymorphism
 */
public class PaymentRepository implements Repository<Payment> {
    
    /**
     * Find all payments
     * @return List of all payments
     */
    @Override
    public List<Payment> findAll() {
        try {
            Type type = FileUtils.getListType(Payment.class);
            List<Payment> payments = FileUtils.readListFromJson(Constants.PAYMENTS_FILE, type);
            
            return payments != null ? payments : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error reading payments: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Find a payment by ID
     * @param id Payment ID
     * @return Payment or null if not found
     */
    @Override
    public Payment findById(String id) {
        List<Payment> payments = findAll();
        return payments.stream()
                .filter(payment -> payment.getPaymentID().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Save a new payment
     * @param entity Payment to save
     * @return true if saved successfully
     */
    @Override
    public boolean save(Payment entity) {
        List<Payment> payments = findAll();
        
        // Check for duplicate ID
        if (payments.stream().anyMatch(payment -> payment.getPaymentID().equals(entity.getPaymentID()))) {
            return false;
        }
        
        payments.add(entity);
        return saveList(payments);
    }
    
    /**
     * Update an existing payment
     * @param entity Payment to update
     * @return true if updated successfully
     */
    @Override
    public boolean update(Payment entity) {
        List<Payment> payments = findAll();
        
        boolean found = false;
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getPaymentID().equals(entity.getPaymentID())) {
                payments.set(i, entity);
                found = true;
                break;
            }
        }
        
        return found && saveList(payments);
    }
    
    /**
     * Delete a payment
     * @param id Payment ID
     * @return true if deleted successfully
     */
    @Override
    public boolean delete(String id) {
        List<Payment> payments = findAll();
        int originalSize = payments.size();
        
        payments = payments.stream()
                .filter(payment -> !payment.getPaymentID().equals(id))
                .collect(Collectors.toList());
        
        return (payments.size() < originalSize) && saveList(payments);
    }
    
    /**
     * Find payments by purchase order ID
     * @param poId Purchase order ID
     * @return List of payments for the specified PO
     */
    public List<Payment> findByPurchaseOrder(String poId) {
        return findAll().stream()
                .filter(payment -> payment.getPoID().equals(poId))
                .collect(Collectors.toList());
    }
    
    /**
     * Find payments by supplier ID
     * @param supplierId Supplier ID
     * @return List of payments for the specified supplier
     */
    public List<Payment> findBySupplier(String supplierId) {
        return findAll().stream()
                .filter(payment -> payment.getSupplierID().equals(supplierId))
                .collect(Collectors.toList());
    }
    
    /**
     * Find payments by status
     * @param status Payment status
     * @return List of payments with the specified status
     */
    public List<Payment> findByStatus(Payment.Status status) {
        return findAll().stream()
                .filter(payment -> payment.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    /**
     * Generate a new unique payment ID
     * @return New payment ID
     */
    public String generateNewPaymentID() {
        List<Payment> payments = findAll();
        
        if (payments.isEmpty()) {
            return "PAY001";
        }
        
        // Find the highest payment ID number
        int maxId = payments.stream()
                .map(payment -> Integer.parseInt(payment.getPaymentID().substring(3)))
                .max(Integer::compare)
                .orElse(0);
        
        // Generate the next ID
        return String.format("PAY%03d", maxId + 1);
    }
    
    /**
     * Save the list of payments to the file
     * @param payments List of payments to save
     * @return true if saved successfully
     */
    private boolean saveList(List<Payment> payments) {
        try {
            FileUtils.writeListToJson(Constants.PAYMENTS_FILE, payments);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving payments: " + e.getMessage());
            return false;
        }
    }
}