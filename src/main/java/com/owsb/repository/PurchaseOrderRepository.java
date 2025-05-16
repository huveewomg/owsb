package com.owsb.repository;

import com.owsb.model.procurement.PurchaseOrder;
import com.owsb.util.Constants;
import com.owsb.util.FileUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for PurchaseOrder entities
 * Implements the Repository interface demonstrating polymorphism
 */
public class PurchaseOrderRepository implements Repository<PurchaseOrder> {
    
    /**
     * Find all purchase orders
     * @return List of all purchase orders
     */
    @Override
    public List<PurchaseOrder> findAll() {
        try {
            Type type = FileUtils.getListType(PurchaseOrder.class);
            List<PurchaseOrder> orders = FileUtils.readListFromJson(Constants.PO_FILE, type);
            
            return orders != null ? orders : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error reading purchase orders: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Find a purchase order by ID
     * @param id Purchase order ID
     * @return PurchaseOrder or null if not found
     */
    @Override
    public PurchaseOrder findById(String id) {
        List<PurchaseOrder> orders = findAll();
        return orders.stream()
                .filter(po -> po.getPoID().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Save a new purchase order
     * @param entity Purchase order to save
     * @return true if saved successfully
     */
    @Override
    public boolean save(PurchaseOrder entity) {
        List<PurchaseOrder> orders = findAll();
        
        // Check for duplicate ID
        if (orders.stream().anyMatch(po -> po.getPoID().equals(entity.getPoID()))) {
            return false;
        }
        
        orders.add(entity);
        return saveList(orders);
    }
    
    /**
     * Update an existing purchase order
     * @param entity Purchase order to update
     * @return true if updated successfully
     */
    @Override
    public boolean update(PurchaseOrder entity) {
        List<PurchaseOrder> orders = findAll();
        
        boolean found = false;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getPoID().equals(entity.getPoID())) {
                orders.set(i, entity);
                found = true;
                break;
            }
        }
        
        return found && saveList(orders);
    }
    
    /**
     * Delete a purchase order
     * @param id Purchase order ID
     * @return true if deleted successfully
     */
    @Override
    public boolean delete(String id) {
        List<PurchaseOrder> orders = findAll();
        int originalSize = orders.size();
        
        orders = orders.stream()
                .filter(po -> !po.getPoID().equals(id))
                .collect(Collectors.toList());
        
        return (orders.size() < originalSize) && saveList(orders);
    }
    
    /**
     * Find purchase orders by purchase manager ID
     * @param purchaseManagerID ID of the purchase manager
     * @return List of POs created by the specified purchase manager
     */
    public List<PurchaseOrder> findByPurchaseManager(String purchaseManagerID) {
        return findAll().stream()
                .filter(po -> po.getPurchaseManagerID().equals(purchaseManagerID))
                .collect(Collectors.toList());
    }
    
    /**
     * Find purchase orders by status
     * @param status Status to filter by
     * @return List of POs with the specified status
     */
    public List<PurchaseOrder> findByStatus(Constants.PurchaseOrderStatus status) {
        return findAll().stream()
                .filter(po -> po.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    /**
     * Find purchase orders by PR ID
     * @param prID PR ID to filter by
     * @return List of POs associated with the specified PR
     */
    public List<PurchaseOrder> findByPR(String prID) {
        return findAll().stream()
                .filter(po -> po.getPrID().equals(prID))
                .collect(Collectors.toList());
    }
    
    /**
     * Generate a new unique PO ID
     * @return New PO ID
     */
    public String generateNewPOID() {
        List<PurchaseOrder> orders = findAll();
        
        if (orders.isEmpty()) {
            return "PO001";
        }
        
        // Find the highest PO ID number
        int maxId = orders.stream()
                .map(po -> Integer.parseInt(po.getPoID().substring(2)))
                .max(Integer::compare)
                .orElse(0);
        
        // Generate the next ID
        return String.format("PO%03d", maxId + 1);
    }
    
    /**
     * Save the list of purchase orders to the file
     * @param orders List of purchase orders to save
     * @return true if saved successfully
     */
    private boolean saveList(List<PurchaseOrder> orders) {
        try {
            FileUtils.writeListToJson(Constants.PO_FILE, orders);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving purchase orders: " + e.getMessage());
            return false;
        }
    }
}