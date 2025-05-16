package com.owsb.repository;

import com.owsb.model.procurement.PRItem;
import com.owsb.model.procurement.PurchaseRequisition;
import com.owsb.util.Constants;
import com.owsb.util.FileUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for PurchaseRequisition entities
 * Implements the Repository interface demonstrating polymorphism
 */
public class PurchaseRequisitionRepository implements Repository<PurchaseRequisition> {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * Find all purchase requisitions
     * @return List of all purchase requisitions
     */
    @Override
    public List<PurchaseRequisition> findAll() {
        try {
            Type type = FileUtils.getListType(PurchaseRequisition.class);
            List<PurchaseRequisition> requisitions = FileUtils.readListFromJson(Constants.PR_FILE, type);
            
            return requisitions != null ? requisitions : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error reading purchase requisitions: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Find a purchase requisition by ID
     * @param id Purchase requisition ID
     * @return PurchaseRequisition or null if not found
     */
    @Override
    public PurchaseRequisition findById(String id) {
        List<PurchaseRequisition> requisitions = findAll();
        return requisitions.stream()
                .filter(pr -> pr.getPrID().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Save a new purchase requisition
     * @param entity Purchase requisition to save
     * @return true if saved successfully
     */
    @Override
    public boolean save(PurchaseRequisition entity) {
        List<PurchaseRequisition> requisitions = findAll();
        
        // Check for duplicate ID
        if (requisitions.stream().anyMatch(pr -> pr.getPrID().equals(entity.getPrID()))) {
            return false;
        }
        
        requisitions.add(entity);
        return saveList(requisitions);
    }
    
    /**
     * Update an existing purchase requisition
     * @param entity Purchase requisition to update
     * @return true if updated successfully
     */
    @Override
    public boolean update(PurchaseRequisition entity) {
        List<PurchaseRequisition> requisitions = findAll();
        
        boolean found = false;
        for (int i = 0; i < requisitions.size(); i++) {
            if (requisitions.get(i).getPrID().equals(entity.getPrID())) {
                requisitions.set(i, entity);
                found = true;
                break;
            }
        }
        
        return found && saveList(requisitions);
    }
    
    /**
     * Delete a purchase requisition
     * @param id Purchase requisition ID
     * @return true if deleted successfully
     */
    @Override
    public boolean delete(String id) {
        List<PurchaseRequisition> requisitions = findAll();
        int originalSize = requisitions.size();
        
        requisitions = requisitions.stream()
                .filter(pr -> !pr.getPrID().equals(id))
                .collect(Collectors.toList());
        
        return (requisitions.size() < originalSize) && saveList(requisitions);
    }
    
    /**
     * Find purchase requisitions by salesManagerID
     * @param salesManagerID ID of the sales manager
     * @return List of PRs created by the specified sales manager
     */
    public List<PurchaseRequisition> findBySalesManager(String salesManagerID) {
        return findAll().stream()
                .filter(pr -> pr.getSalesManagerID().equals(salesManagerID))
                .collect(Collectors.toList());
    }
    
    /**
     * Find purchase requisitions by status
     * @param status Status to filter by
     * @return List of PRs with the specified status
     */
    public List<PurchaseRequisition> findByStatus(Constants.PurchaseRequisitionStatus status) {
        return findAll().stream()
                .filter(pr -> pr.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    /**
     * Generate a new unique PR ID
     * @return New PR ID
     */
    public String generateNewPRID() {
        List<PurchaseRequisition> requisitions = findAll();
        
        if (requisitions.isEmpty()) {
            return "PR001";
        }
        
        // Find the highest PR ID number
        int maxId = requisitions.stream()
                .map(pr -> Integer.parseInt(pr.getPrID().substring(2)))
                .max(Integer::compare)
                .orElse(0);
        
        // Generate the next ID
        return String.format("PR%03d", maxId + 1);
    }
    
    /**
     * Save the list of purchase requisitions to the file
     * @param requisitions List of purchase requisitions to save
     * @return true if saved successfully
     */
    private boolean saveList(List<PurchaseRequisition> requisitions) {
        try {
            // FileUtils.createBackup(Constants.PR_FILE);
            FileUtils.writeListToJson(Constants.PR_FILE, requisitions);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving purchase requisitions: " + e.getMessage());
            return false;
        }
    }
}