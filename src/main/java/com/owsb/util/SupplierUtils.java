package com.owsb.util;

import com.owsb.model.Supplier;
import com.owsb.repository.SupplierRepository;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for supplier-related operations
 * Provides methods for loading suppliers and populating UI components
 */
public class SupplierUtils {
    private static final SupplierRepository supplierRepository = new SupplierRepository();
    
    /**
     * Get a map of supplier IDs to supplier names
     * @return Map with supplier ID as key and supplier name as value
     */
    public static Map<String, String> getSupplierIdToNameMap() {
        Map<String, String> supplierMap = new HashMap<>();
        List<Supplier> suppliers = supplierRepository.findAll();
        
        for (Supplier supplier : suppliers) {
            supplierMap.put(supplier.getSupplierID(), supplier.getName());
        }
        
        return supplierMap;
    }
    
    /**
     * Get a map of supplier names to supplier IDs
     * @return Map with supplier name as key and supplier ID as value
     */
    public static Map<String, String> getSupplierNameToIdMap() {
        Map<String, String> supplierMap = new HashMap<>();
        List<Supplier> suppliers = supplierRepository.findAll();
        
        for (Supplier supplier : suppliers) {
            supplierMap.put(supplier.getName(), supplier.getSupplierID());
        }
        
        return supplierMap;
    }
    
    /**
     * Populate a combo box with supplier names
     * @param comboBox The combo box to populate
     */
    public static void populateSupplierComboBox(JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        
        List<Supplier> suppliers = supplierRepository.findAll();
        
        for (Supplier supplier : suppliers) {
            comboBox.addItem(supplier.getName());
        }
        
        // Set default selection if items exist
        if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(0);
        }
    }
    
    /**
     * Get supplier ID from the name selected in a combo box
     * @param comboBox The combo box containing supplier names
     * @return The ID of the selected supplier or null if none selected
     */
    public static String getSelectedSupplierId(JComboBox<String> comboBox) {
        String selectedName = (String) comboBox.getSelectedItem();
        if (selectedName == null || selectedName.isEmpty()) {
            return null;
        }
        
        return getSupplierNameToIdMap().get(selectedName);
    }
    
    /**
     * Get all suppliers from the repository
     * @return List of suppliers
     */
    public static List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }
    
    /**
     * Get supplier by ID
     * @param supplierId The supplier ID
     * @return The supplier or null if not found
     */
    public static Supplier getSupplierById(String supplierId) {
        return supplierRepository.findById(supplierId);
    }
}