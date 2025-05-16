package com.owsb.controller;

import com.owsb.model.supplier.Supplier;
import com.owsb.service.SupplierService;
import java.util.List;

/**
 * Extension to PurchaseOrderController to handle supplier selection
 * Adds methods for finding alternative suppliers for items
 */
public class SupplierSelectionController {
    private final SupplierService supplierService;
    
    /**
     * Constructor
     */
    public SupplierSelectionController() {
        this.supplierService = new SupplierService();
    }
    
    /**
     * Find all suppliers that can supply a given item
     * @param itemID Item ID to find suppliers for
     * @return List of suppliers that can supply the item
     */
    public List<Supplier> getAlternativeSuppliersForItem(String itemID) {
        return supplierService.findSuppliersForItem(itemID);
    }
    
    /**
     * Get primary supplier for an item
     * @param itemID Item ID
     * @return Primary supplier or null if not found
     */
    public Supplier getPrimarySupplierForItem(String itemID) {
        return supplierService.getPrimarySupplierForItem(itemID);
    }
}