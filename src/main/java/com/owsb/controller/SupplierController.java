package com.owsb.controller;

import com.owsb.model.inventory.Item;
import com.owsb.model.supplier.Supplier;
import com.owsb.model.user.User;
import com.owsb.repository.ItemRepository;
import com.owsb.repository.SupplierRepository;
import com.owsb.util.UserRole;

import java.util.ArrayList;
import java.util.List;

public class SupplierController {
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private User currentUser;

    public SupplierController() {
        this.supplierRepository = new SupplierRepository();
        this.itemRepository = new ItemRepository();
    }
    
    /**
     * Set the current user for access control
     * @param user Current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public String generateNextSupplierId() {
        return supplierRepository.generateSupplierId();
    }

    public boolean addSupplier(String name, String contactPerson, String phone) {
        if (name == null || name.trim().isEmpty() ||
            contactPerson == null || contactPerson.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Check access permission if user is set
        if (currentUser != null && 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        String supplierId = supplierRepository.generateSupplierId();
        Supplier supplier = new Supplier(supplierId, name, contactPerson, phone);
        
        // Initialize with empty itemIDs list
        if (supplier.getItemIDs() == null) {
            supplier.setItemIDs(new ArrayList<>());
        }
        
        return supplierRepository.save(supplier);
    }

    public boolean updateSupplier(String supplierId, String name, String contactPerson, String phone) {
        if (supplierId == null || supplierId.trim().isEmpty() ||
            name == null || name.trim().isEmpty() ||
            contactPerson == null || contactPerson.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Check access permission if user is set
        if (currentUser != null && 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        Supplier supplier = supplierRepository.findById(supplierId);
        if (supplier == null) {
            return false;
        }
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        return supplierRepository.update(supplier);
    }

    public boolean deleteSupplier(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return false;
        }
        
        // Check access permission if user is set
        if (currentUser != null && 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        // Check if supplier is the primary supplier for any items
        Supplier supplier = supplierRepository.findById(supplierId);
        if (supplier != null) {
            List<Item> items = itemRepository.findBySupplier(supplierId);
            if (!items.isEmpty()) {
                // Can't delete supplier that is the primary supplier for items
                return false;
            }
        }
        
        return supplierRepository.delete(supplierId);
    }

    public Supplier getSupplierById(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return null;
        }
        return supplierRepository.findById(supplierId);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }
    
    /**
     * Get all suppliers that can supply a specific item
     * @param itemId Item ID
     * @return List of suppliers that can supply the item
     */
    public List<Supplier> getSuppliersForItem(String itemId) {
        List<Supplier> result = new ArrayList<>();
        
        for (Supplier supplier : supplierRepository.findAll()) {
            List<String> itemIds = supplier.getItemIDs();
            if (itemIds != null && itemIds.contains(itemId)) {
                result.add(supplier);
            }
        }
        
        return result;
    }
    
    /**
     * Add an item to a supplier's catalog
     * @param supplierId Supplier ID
     * @param itemId Item ID
     * @return true if successful, false otherwise
     */
    public boolean addItemToSupplier(String supplierId, String itemId) {
        // Check access permission if user is set
        if (currentUser != null && 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        // Validate input
        if (supplierId == null || supplierId.trim().isEmpty() ||
            itemId == null || itemId.trim().isEmpty()) {
            return false;
        }
        
        // Check if item exists
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            return false;
        }
        
        // Get supplier
        Supplier supplier = supplierRepository.findById(supplierId);
        if (supplier == null) {
            return false;
        }
        
        // Initialize itemIDs list if null
        if (supplier.getItemIDs() == null) {
            supplier.setItemIDs(new ArrayList<>());
        }
        
        // Add item to supplier if not already there
        if (!supplier.getItemIDs().contains(itemId)) {
            supplier.getItemIDs().add(itemId);
        } else {
            // Item already in supplier's catalog, still return true
            return true;
        }
        
        // If item has no primary supplier, set this supplier as primary
        if (item.getSupplierID() == null || item.getSupplierID().isEmpty()) {
            item.setSupplierID(supplierId);
            itemRepository.update(item);
        }
        
        return supplierRepository.update(supplier);
    }
    
    /**
     * Remove an item from a supplier's catalog
     * @param supplierId Supplier ID
     * @param itemId Item ID
     * @return true if successful, false otherwise
     */
    public boolean removeItemFromSupplier(String supplierId, String itemId) {
        // Check access permission if user is set
        if (currentUser != null && 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        // Validate input
        if (supplierId == null || supplierId.trim().isEmpty() ||
            itemId == null || itemId.trim().isEmpty()) {
            return false;
        }
        
        // Get supplier
        Supplier supplier = supplierRepository.findById(supplierId);
        if (supplier == null) {
            return false;
        }
        
        // Check if item exists in supplier's catalog
        List<String> itemIds = supplier.getItemIDs();
        if (itemIds == null || !itemIds.contains(itemId)) {
            return false;
        }
        
        // Check if this is the primary supplier for the item
        Item item = itemRepository.findById(itemId);
        if (item != null && supplierId.equals(item.getSupplierID())) {
            // Find alternative suppliers for this item
            List<Supplier> alternativeSuppliers = new ArrayList<>();
            for (Supplier s : supplierRepository.findAll()) {
                if (!s.getSupplierID().equals(supplierId) && 
                    s.getItemIDs() != null && 
                    s.getItemIDs().contains(itemId)) {
                    alternativeSuppliers.add(s);
                }
            }
            
            if (!alternativeSuppliers.isEmpty()) {
                // Set the first alternative supplier as primary
                item.setSupplierID(alternativeSuppliers.get(0).getSupplierID());
                itemRepository.update(item);
            } else {
                // No alternative suppliers, clear the primary supplier
                item.setSupplierID("");
                itemRepository.update(item);
            }
        }
        
        // Remove item from supplier
        itemIds.remove(itemId);
        
        return supplierRepository.update(supplier);
    }
}