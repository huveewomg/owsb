package com.owsb.controller;

import com.owsb.model.Item;
import com.owsb.model.PRItem;
import com.owsb.model.PurchaseRequisition;
import com.owsb.model.Supplier;
import com.owsb.model.User;
import com.owsb.repository.ItemRepository;
import com.owsb.repository.PurchaseRequisitionRepository;
import com.owsb.repository.SupplierRepository;
import com.owsb.model.PurchaseRequisition.Status;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for purchase requisition operations
 * Demonstrates the MVC pattern by separating business logic from UI
 */
public class PurchaseRequisitionController {
    private final PurchaseRequisitionRepository prRepository;
    private final ItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    
    private User currentUser;
    
    // Constants for business rules
    public static final int MINIMUM_ITEMS_REQUIRED = 3;
    
    /**
     * Constructor for PurchaseRequisitionController
     */
    public PurchaseRequisitionController() {
        this.prRepository = new PurchaseRequisitionRepository();
        this.itemRepository = new ItemRepository();
        this.supplierRepository = new SupplierRepository();
    }
    
    /**
     * Set the current user
     * @param user Current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Get the current user
     * @return Current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get all purchase requisitions
     * @return List of all purchase requisitions
     */
    public List<PurchaseRequisition> getAllPurchaseRequisitions() {
        return prRepository.findAll();
    }
    
    /**
     * Get all purchase requisition IDs
     * @return List of PR IDs
     */
    public List<String> getAllPurchaseRequisitionIds() {
        List<PurchaseRequisition> prs = prRepository.findAll();
        List<String> ids = new ArrayList<>();
        for (PurchaseRequisition pr : prs) {
            ids.add(pr.getPrID());
        }
        return ids;
    }
    
    /**
     * Get purchase requisitions by status
     * @param status Status to filter by
     * @return List of PRs with the specified status
     */
    public List<PurchaseRequisition> getPurchaseRequisitionsByStatus(Status status) {
        return prRepository.findByStatus(status);
    }
    
    /**
     * Get purchase requisitions created by the current sales manager
     * @return List of PRs created by the current sales manager
     */
    public List<PurchaseRequisition> getMyPurchaseRequisitions() {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        return prRepository.findBySalesManager(currentUser.getUserId());
    }
    
    /**
     * Create a new purchase requisition
     * @param requiredDate Required delivery date
     * @param notes Optional notes
     * @param items List of PR items
     * @param isUrgent Whether the PR is urgent (bypasses minimum item check)
     * @return true if created successfully
     */
    public boolean createPurchaseRequisition(Date requiredDate, String notes, 
                                           List<PRItem> items, boolean isUrgent) {
        // Validate inputs
        if (requiredDate == null || items == null || items.isEmpty()) {
            return false;
        }
        
        // Check if current user is set
        if (currentUser == null) {
            return false;
        }
        
        // For drafts, we don't check the minimum items requirement
        // Draft PRs are created with NEW status, and items check happens at submit time
        
        // Generate a new PR ID
        String prId = prRepository.generateNewPRID();
        
        // Create a new purchase requisition
        PurchaseRequisition pr = new PurchaseRequisition(
                prId,
                new Date(), // Current date
                requiredDate,
                currentUser.getUserId(),
                Status.NEW,
                notes,
                items
        );
        
        // Save the purchase requisition
        return prRepository.save(pr);
    }
    
    /**
     * Update an existing purchase requisition
     * @param prId PR ID
     * @param requiredDate Required delivery date
     * @param notes Optional notes
     * @param items List of PR items
     * @param isUrgent Whether the PR is urgent (bypasses minimum item check)
     * @return true if updated successfully
     */
    public boolean updatePurchaseRequisition(String prId, Date requiredDate, String notes, 
                                           List<PRItem> items, boolean isUrgent) {
        // Validate inputs
        if (prId == null || prId.isEmpty() || requiredDate == null || 
            items == null || items.isEmpty()) {
            return false;
        }
        
        // Get the purchase requisition
        PurchaseRequisition pr = prRepository.findById(prId);
        if (pr == null) {
            return false;
        }
        
        // Check if PR can be updated (only if status is NEW)
        if (pr.getStatus() != Status.NEW) {
            return false;
        }
        
        // For drafts, we don't check the minimum items requirement
        // Minimum items check happens at submit time
        
        // Update the purchase requisition
        pr.setRequiredDate(requiredDate);
        pr.setNotes(notes);
        pr.setItems(items);
        
        // Save the updated purchase requisition
        return prRepository.update(pr);
    }
    
    /**
     * Delete a purchase requisition
     * @param prId PR ID
     * @return true if deleted successfully
     */
    public boolean deletePurchaseRequisition(String prId) {
        // Get the purchase requisition
        PurchaseRequisition pr = prRepository.findById(prId);
        if (pr == null) {
            return false;
        }
        
        // Check if PR can be deleted (only if status is NEW)
        if (pr.getStatus() != Status.NEW) {
            return false;
        }
        
        // Delete the purchase requisition
        return prRepository.delete(prId);
    }
    
    /**
     * Submit a purchase requisition for approval
     * @param prId PR ID
     * @return true if submitted successfully
     */
    public boolean submitPurchaseRequisition(String prId) {
        // Get the purchase requisition
        PurchaseRequisition pr = prRepository.findById(prId);
        if (pr == null) {
            return false;
        }
        
        // Check if PR can be submitted (only if status is NEW)
        if (pr.getStatus() != Status.NEW) {
            return false;
        }
        
        // Update the status
        pr.setStatus(Status.PENDING_APPROVAL);
        
        // Save the updated purchase requisition
        return prRepository.update(pr);
    }
    
    /**
     * Change the status of a purchase requisition
     * @param prId PR ID
     * @param status New status
     * @return true if status changed successfully
     */
    public boolean changePurchaseRequisitionStatus(String prId, Status status) {
        // Get the purchase requisition
        PurchaseRequisition pr = prRepository.findById(prId);
        if (pr == null) {
            return false;
        }
        
        // Update the status
        pr.setStatus(status);
        
        // Save the updated purchase requisition
        return prRepository.update(pr);
    }
    
    /**
     * Get all items with low stock (below minimum stock level)
     * @return List of items with low stock
     */
    public List<Item> getItemsWithLowStock() {
        List<Item> allItems = itemRepository.findAll();
        
        return allItems.stream()
                .filter(item -> item.getCurrentStock() < item.getMinimumStock())
                .collect(Collectors.toList());
    }
    
    /**
     * Create a PR item from an item
     * @param item Item to create PR item from
     * @param quantity Quantity to order
     * @return PR item
     */
    public PRItem createPRItemFromItem(Item item, int quantity) {
        // Get the supplier
        Supplier supplier = supplierRepository.findById(item.getSupplierID());
        
        // Calculate default required date (2 weeks from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 14);
        Date requiredDate = calendar.getTime();
        
        // Create a new PR item
        return new PRItem(
                item.getItemID(),
                item.getName(),
                quantity,
                requiredDate,
                supplier != null ? supplier.getSupplierID() : null,
                item.getUnitPrice()
        );
    }
    /**
     * Check if an item already has pending purchase requisitions
     * @param itemId Item ID to check
     * @return Information about existing PRs, or null if none exist
     */
    public String checkExistingPendingPR(String itemId) {
        List<PurchaseRequisition> pendingPRs = new ArrayList<>();
        pendingPRs.addAll(prRepository.findByStatus(Status.NEW));
        pendingPRs.addAll(prRepository.findByStatus(Status.PENDING_APPROVAL));
        
        StringBuilder result = new StringBuilder();
        
        for (PurchaseRequisition pr : pendingPRs) {
            for (PRItem item : pr.getItems()) {
                if (item.getItemID().equals(itemId)) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append("PR #").append(pr.getPrID())
                        .append(" (").append(pr.getStatus().getDisplayName())
                        .append(", Qty: ").append(item.getQuantity()).append(")");
                }
            }
        }
        
        return result.length() > 0 ? result.toString() : null;
    }
    
    /**
     * Get suggested order quantity for an item
     * @param item Item to calculate for
     * @return Suggested order quantity
     */
    public int getSuggestedOrderQuantity(Item item) {
        // Default strategy: Order to reach maximum stock
        return Math.max(0, item.getMaximumStock() - item.getCurrentStock());
    }
    
    /**
     * Get PR item by item ID from a list of PR items
     * @param items List of PR items
     * @param itemId Item ID to find
     * @return PR item or null if not found
     */
    public PRItem getPRItemByItemId(List<PRItem> items, String itemId) {
        return items.stream()
                .filter(item -> item.getItemID().equals(itemId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get all items
     * @return List of all items
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    
    /**
     * Get an item by ID
     * @param itemId Item ID
     * @return Item or null if not found
     */
    public Item getItemById(String itemId) {
        return itemRepository.findById(itemId);
    }
    
    /**
     * Get a supplier by ID
     * @param supplierId Supplier ID
     * @return Supplier or null if not found
     */
    public Supplier getSupplierById(String supplierId) {
        return supplierRepository.findById(supplierId);
    }
}