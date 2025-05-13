package com.owsb.controller;

import com.owsb.model.POItem;
import com.owsb.model.PRItem;
import com.owsb.model.PurchaseOrder;
import com.owsb.model.PurchaseRequisition;
import com.owsb.model.Supplier;
import com.owsb.model.User;
import com.owsb.repository.PurchaseOrderRepository;
import com.owsb.repository.PurchaseRequisitionRepository;
import com.owsb.repository.SupplierRepository;
import com.owsb.model.PurchaseOrder.Status;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for purchase order operations
 * Demonstrates the MVC pattern by separating business logic from UI
 */
public class PurchaseOrderController {
    private final PurchaseOrderRepository poRepository;
    private final PurchaseRequisitionRepository prRepository;
    private final SupplierRepository supplierRepository;
    
    private User currentUser;
    
    /**
     * Constructor for PurchaseOrderController
     */
    public PurchaseOrderController() {
        this.poRepository = new PurchaseOrderRepository();
        this.prRepository = new PurchaseRequisitionRepository();
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
     * Get all purchase orders
     * @return List of all purchase orders
     */
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return poRepository.findAll();
    }
    
    /**
     * Get purchase orders by status
     * @param status Status to filter by
     * @return List of POs with the specified status
     */
    public List<PurchaseOrder> getPurchaseOrdersByStatus(Status status) {
        return poRepository.findByStatus(status);
    }
    
    /**
     * Get purchase orders created by the current purchase manager
     * @return List of POs created by the current purchase manager
     */
    public List<PurchaseOrder> getMyPurchaseOrders() {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        return poRepository.findByPurchaseManager(currentUser.getUserId());
    }
    
    /**
     * Create a new purchase order from a purchase requisition
     * @param prId Purchase requisition ID
     * @param deliveryDate Expected delivery date
     * @param notes Optional notes
     * @param selectedSuppliers Map of item IDs to selected supplier IDs (can be null)
     * @return true if created successfully
     */
    public boolean createPurchaseOrder(String prId, Date deliveryDate, String notes, 
                                      List<POItem> customItems) {
        // Validate inputs
        if (prId == null || prId.isEmpty() || deliveryDate == null) {
            return false;
        }
        
        // Check if current user is set
        if (currentUser == null) {
            return false;
        }
        
        // Get the purchase requisition
        PurchaseRequisition pr = prRepository.findById(prId);
        if (pr == null) {
            return false;
        }
        
        // Check if PR is in PENDING_APPROVAL status
        if (pr.getStatus() != PurchaseRequisition.Status.PENDING_APPROVAL) {
            return false;
        }
        
        // Generate a new PO ID
        String poId = poRepository.generateNewPOID();
        
        // Create PO items
        List<POItem> poItems = customItems;
        
        // If no custom items were provided, create them from the PR items
        if (poItems == null || poItems.isEmpty()) {
            poItems = new ArrayList<>();
            
            for (PRItem prItem : pr.getItems()) {
                // Get the supplier name
                Supplier supplier = supplierRepository.findById(prItem.getSuggestedSupplierID());
                String supplierName = supplier != null ? supplier.getName() : "Unknown Supplier";
                
                POItem poItem = POItem.fromPRItem(prItem, supplierName);
                poItems.add(poItem);
            }
        }
        
        // Create a new purchase order
        PurchaseOrder po = new PurchaseOrder(
                poId,
                prId,
                new Date(), // Current date
                deliveryDate,
                currentUser.getUserId(),
                Status.PENDING, // Initial status
                notes,
                poItems
        );
        
        // Save the purchase order
        boolean saved = poRepository.save(po);
        
        if (saved) {
            // Update the PR status to PROCESSED
            pr.setStatus(PurchaseRequisition.Status.PROCESSED);
            prRepository.update(pr);
        }
        
        return saved;
    }
    
    /**
     * Update an existing purchase order
     * @param poId PO ID
     * @param deliveryDate Expected delivery date
     * @param notes Optional notes
     * @param items Updated list of PO items
     * @return true if updated successfully
     */
    public boolean updatePurchaseOrder(String poId, Date deliveryDate, String notes, 
                                      List<POItem> items) {
        // Validate inputs
        if (poId == null || poId.isEmpty() || deliveryDate == null || 
            items == null || items.isEmpty()) {
            return false;
        }
        
        // Get the purchase order
        PurchaseOrder po = poRepository.findById(poId);
        if (po == null) {
            return false;
        }
        
        // Check if PO can be updated (only if status is PENDING)
        if (po.getStatus() != Status.PENDING) {
            return false;
        }
        
        // Update the purchase order
        po.setDeliveryDate(deliveryDate);
        po.setNotes(notes);
        po.setItems(items);
        
        // Save the updated purchase order
        return poRepository.update(po);
    }
    
    /**
     * Approve a purchase order (for Finance Manager)
     * @param poId PO ID
     * @return true if approved successfully
     */
    public boolean approvePurchaseOrder(String poId) {
        // Check if current user is set
        if (currentUser == null) {
            return false;
        }
        
        // Get the purchase order
        PurchaseOrder po = poRepository.findById(poId);
        if (po == null) {
            return false;
        }
        
        // Check if PO can be approved (only if status is PENDING)
        if (po.getStatus() != Status.PENDING) {
            return false;
        }
        
        // Update the PO
        po.setStatus(Status.PENDING_ARRIVAL);
        po.setFinanceManagerID(currentUser.getUserId());
        
        // Save the updated PO
        return poRepository.update(po);
    }
    
    /**
     * Reject a purchase order (for Finance Manager)
     * @param poId PO ID
     * @param reason Reason for rejection
     * @return true if rejected successfully
     */
    public boolean rejectPurchaseOrder(String poId, String reason) {
        // Check if current user is set
        if (currentUser == null) {
            return false;
        }
        
        // Get the purchase order
        PurchaseOrder po = poRepository.findById(poId);
        if (po == null) {
            return false;
        }
        
        // Check if PO can be rejected (only if status is PENDING)
        if (po.getStatus() != Status.PENDING) {
            return false;
        }
        
        // Update the PO
        po.setStatus(Status.REJECTED);
        po.setFinanceManagerID(currentUser.getUserId());
        po.setNotes(po.getNotes() + "\n[REJECTED] " + reason);
        
        // Save the updated PO
        return poRepository.update(po);
    }
    
    /**
     * Cancel a purchase order
     * @param poId PO ID
     * @return true if cancelled successfully
     */
    public boolean cancelPurchaseOrder(String poId) {
        // Get the purchase order
        PurchaseOrder po = poRepository.findById(poId);
        if (po == null) {
            return false;
        }
        
        // Check if PO can be cancelled (only if status is PENDING)
        if (po.getStatus() != Status.PENDING) {
            return false;
        }
        
        // Update the PO
        po.setStatus(Status.CANCELLED);
        
        // Save the updated PO
        return poRepository.update(po);
    }
    
    /**
     * Mark a purchase order as received (for Inventory Manager)
     * @param poId PO ID
     * @return true if marked as received successfully
     */
    public boolean markPurchaseOrderReceived(String poId) {
        // Get the purchase order
        PurchaseOrder po = poRepository.findById(poId);
        if (po == null) {
            return false;
        }
        
        // Check if PO can be marked as received (only if status is PENDING_ARRIVAL)
        if (po.getStatus() != Status.PENDING_ARRIVAL) {
            return false;
        }
        
        // Update the PO
        po.setStatus(Status.PENDING_PAYMENT);
        
        // Save the updated PO
        return poRepository.update(po);
    }
    
    /**
     * Mark a purchase order as completed (for Finance Manager)
     * @param poId PO ID
     * @return true if marked as completed successfully
     */
    public boolean completePurchaseOrder(String poId) {
        // Get the purchase order
        PurchaseOrder po = poRepository.findById(poId);
        if (po == null) {
            return false;
        }
        
        // Check if PO can be marked as completed (only if status is PENDING_PAYMENT)
        if (po.getStatus() != Status.PENDING_PAYMENT) {
            return false;
        }
        
        // Update the PO
        po.setStatus(Status.COMPLETED);
        
        // Also update the related PR to COMPLETED
        String prId = po.getPrID();
        if (prId != null && !prId.isEmpty()) {
            PurchaseRequisition pr = prRepository.findById(prId);
            if (pr != null) {
                pr.setStatus(PurchaseRequisition.Status.COMPLETED);
                prRepository.update(pr);
            }
        }
        
        // Save the updated PO
        return poRepository.update(po);
    }
    
    /**
     * Get purchase orders by PR ID
     * @param prId PR ID to filter by
     * @return List of POs associated with the specified PR
     */
    public List<PurchaseOrder> getPurchaseOrdersByPR(String prId) {
        return poRepository.findByPR(prId);
    }
    
    /**
     * Get a purchase requisition by ID
     * @param prId PR ID
     * @return Purchase requisition or null if not found
     */
    public PurchaseRequisition getPurchaseRequisition(String prId) {
        return prRepository.findById(prId);
    }
    
    /**
     * Get a supplier's name by ID
     * @param supplierId Supplier ID
     * @return Supplier name or "Unknown Supplier" if not found
     */
    public String getSupplierName(String supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId);
        return supplier != null ? supplier.getName() : "Unknown Supplier";
    }
    
    /**
     * Get all purchase requisitions with PENDING_APPROVAL status
     * @return List of PRs with PENDING_APPROVAL status
     */
    public List<PurchaseRequisition> getPendingApprovalPRs() {
        return prRepository.findByStatus(PurchaseRequisition.Status.PENDING_APPROVAL);
    }
}