package com.owsb.service;

import com.owsb.model.supplier.Supplier;
import com.owsb.repository.SupplierRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for supplier-related operations
 * Implements business logic for finding alternative suppliers
 */
public class SupplierService {
    private final SupplierRepository supplierRepository;
    
    /**
     * Constructor
     * @param supplierRepository Supplier repository
     */
    public SupplierService() {
        this.supplierRepository = new SupplierRepository();
    }
    
    /**
     * Find all suppliers that can supply a given item
     * @param itemID Item ID to find suppliers for
     * @return List of suppliers that can supply the item
     */
    public List<Supplier> findSuppliersForItem(String itemID) {
        List<Supplier> allSuppliers = supplierRepository.findAll();
        List<Supplier> matchingSuppliers = new ArrayList<>();
        
        // Scan all suppliers to find those that can supply this item
        for (Supplier supplier : allSuppliers) {
            List<String> suppliedItems = supplier.getItemIDs();
            if (suppliedItems != null && suppliedItems.contains(itemID)) {
                matchingSuppliers.add(supplier);
            }
        }
        
        return matchingSuppliers;
    }
    
    /**
     * Get primary supplier for an item
     * @param itemID Item ID
     * @return Primary supplier or null if not found
     */
    public Supplier getPrimarySupplierForItem(String itemID) {
        // This uses the existing relationship where each item has one primary supplier
        List<Supplier> suppliers = findSuppliersForItem(itemID);
        
        // If we have suppliers, return the first one as primary
        if (!suppliers.isEmpty()) {
            return suppliers.get(0);
        }
        
        return null;
    }
}