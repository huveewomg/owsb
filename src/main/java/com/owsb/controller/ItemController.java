package com.owsb.controller;

import com.owsb.model.inventory.Item;
import com.owsb.model.user.User;
import com.owsb.model.supplier.Supplier;
import com.owsb.repository.ItemRepository;
import com.owsb.repository.SupplierRepository;
import com.owsb.util.UserRole;

import java.util.List;

/**
 * Controller for Item-related operations
 * Demonstrates separation of UI and business logic (MVC pattern)
 * Also demonstrates role-based access control
 */
public class ItemController {
    private final ItemRepository itemRepository;
    private final SupplierRepository supplierRepository = new SupplierRepository();
    private User currentUser;
    
    /**
     * Constructor initializes the repository
     */
    public ItemController() {
        this.itemRepository = new ItemRepository();
    }
    
    /**
     * Set the current user for access control
     * @param user Current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Get all items in the system
     * @return List of all items
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    
    /**
     * Find a specific item by ID
     * @param id Item ID to find
     * @return Item if found, null otherwise
     */
    public Item getItemById(String id) {
        return itemRepository.findById(id);
    }
    
    /**
     * Add a new item to the system
     * @return true if successful, false otherwise
     */
    public boolean addItem(String name, String description, double unitPrice, 
                          String category, String supplierID) {
        // Check access permission - only Sales Manager and Admin can add items
        if (currentUser == null || 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        // Validate input
        if (name == null || name.trim().isEmpty() ||
            description == null || description.trim().isEmpty() ||
            unitPrice <= 0 ||
            category == null || category.trim().isEmpty() ||
            supplierID == null || supplierID.trim().isEmpty()) {
            return false;
        }
        
        // Create and save new item
        String itemId = itemRepository.generateItemId();
        Item newItem = new Item(itemId, name, description, unitPrice, category, supplierID);
        boolean itemSaved = itemRepository.save(newItem);
        if (itemSaved) {
            // Add itemID to supplier's itemIDs list
            Supplier supplier = supplierRepository.findById(supplierID);
            if (supplier != null) {
                supplier.addItem(itemId);
                supplierRepository.update(supplier);
            }
        }
        return itemSaved;
    }
    
    /**
     * Update an existing item
     * @return true if successful, false otherwise
     */
    public boolean updateItem(String itemId, String name, String description, 
                             double unitPrice, String category, String supplierID) {
        // Check access permission
        if (currentUser == null || 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        // Validate input
        if (itemId == null || itemId.trim().isEmpty() ||
            name == null || name.trim().isEmpty() ||
            description == null || description.trim().isEmpty() ||
            unitPrice <= 0 ||
            category == null || category.trim().isEmpty() ||
            supplierID == null || supplierID.trim().isEmpty()) {
            return false;
        }
        
        // Get existing item
        Item existingItem = itemRepository.findById(itemId);
        if (existingItem == null) {
            return false;
        }
        String oldSupplierId = existingItem.getSupplierID();
        // Update item properties
        existingItem.setName(name);
        existingItem.setDescription(description);
        existingItem.setUnitPrice(unitPrice);
        existingItem.setCategory(category);
        existingItem.setSupplierID(supplierID);
        boolean updated = itemRepository.update(existingItem);
        if (!updated) return false;
        // If supplier changed, update suppliers.txt
        if (!oldSupplierId.equals(supplierID)) {
            Supplier oldSupplier = supplierRepository.findById(oldSupplierId);
            if (oldSupplier != null) {
                oldSupplier.removeItem(itemId);
                supplierRepository.update(oldSupplier);
            }
            Supplier newSupplier = supplierRepository.findById(supplierID);
            if (newSupplier != null) {
                newSupplier.addItem(itemId);
                supplierRepository.update(newSupplier);
            }
        }
        return true;
    }

    /**
     * Overloaded updateItem for UI (without supplierID, keeps existing supplierID)
     */
    public boolean updateItem(String itemId, String name, String description, double unitPrice, String category) {
        // Check access permission
        if (currentUser == null || 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        // Validate input
        if (itemId == null || itemId.trim().isEmpty() ||
            name == null || name.trim().isEmpty() ||
            description == null || description.trim().isEmpty() ||
            unitPrice <= 0 ||
            category == null || category.trim().isEmpty()) {
            return false;
        }
        // Get existing item
        Item existingItem = itemRepository.findById(itemId);
        if (existingItem == null) {
            return false;
        }
        // Update item properties (keep supplierID unchanged)
        existingItem.setName(name);
        existingItem.setDescription(description);
        existingItem.setUnitPrice(unitPrice);
        existingItem.setCategory(category);
        return itemRepository.update(existingItem);
    }
    
    /**
     * Delete an item from the system
     * @param itemId Item ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteItem(String itemId) {
        // Check access permission
        if (currentUser == null || 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        // Get the item before deleting
        Item item = itemRepository.findById(itemId);
        boolean deleted = itemRepository.delete(itemId);
        if (deleted && item != null) {
            // Remove itemID from supplier's itemIDs list
            Supplier supplier = supplierRepository.findById(item.getSupplierID());
            if (supplier != null) {
                supplier.removeItem(itemId);
                supplierRepository.update(supplier);
            }
        }
        return deleted;
    }
    
    /**
     * Update the stock level of an item
     * @param itemId Item ID to update
     * @param quantity Change in quantity
     * @return true if successful, false otherwise
     */
    public boolean updateItemStock(String itemId, int quantity) {
        // Check access permission - only Inventory Manager and Admin can update stock
        if (currentUser == null || 
            !(currentUser.getRole() == UserRole.INVENTORY_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        return itemRepository.updateStock(itemId, quantity);
    }
    
    /**
     * Get items with stock below minimum levels
     * @return List of items that need reordering
     */
    public List<Item> getLowStockItems() {
        return itemRepository.findLowStockItems();
    }
    
    /**
     * Get items from a specific supplier
     * @param supplierID Supplier ID
     * @return List of items from that supplier
     */
    public List<Item> getItemsBySupplier(String supplierID) {
        return itemRepository.findBySupplier(supplierID);
    }
    
    /**
     * Check if current user can see stock information
     * Only Inventory Manager, Purchase Manager, and Admin can see stock
     * @return true if user can see stock, false otherwise
     */
    public boolean canViewStock() {
        if (currentUser == null) {
            return false;
        }
        
        return currentUser.getRole() == UserRole.INVENTORY_MANAGER ||
               currentUser.getRole() == UserRole.PURCHASE_MANAGER ||
               currentUser.getRole() == UserRole.ADMIN;
    }
    
    /**
     * Check if current user can manage stock
     * Only Inventory Manager and Admin can manage stock
     * @return true if user can manage stock, false otherwise
     */
    public boolean canManageStock() {
        if (currentUser == null) {
            return false;
        }
        
        return currentUser.getRole() == UserRole.INVENTORY_MANAGER ||
               currentUser.getRole() == UserRole.ADMIN;
    }
    
    /**
     * Process sales transaction
     * Reduces stock and records sale
     * @param itemId Item sold
     * @param quantity Quantity sold
     * @return true if successful, false otherwise
     */
    public boolean processSale(String itemId, int quantity) {
        // Check access permission - only Sales Manager and Admin can process sales
        if (currentUser == null || 
            !(currentUser.getRole() == UserRole.SALES_MANAGER || 
              currentUser.getRole() == UserRole.ADMIN)) {
            return false;
        }
        
        // Check if item exists
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            return false;
        }
        
        // Check if enough stock
        if (item.getCurrentStock() < quantity) {
            return false;
        }
        
        // Reduce stock (negative quantity because it's a reduction)
        return itemRepository.updateStock(itemId, -quantity);
        
        // In a real system, we would also record the sale in a sales repository
    }

    /**
     * Generate the next item ID for UI display
     * @return Next available item ID
     */
    public String generateNextItemId() {
        return itemRepository.generateItemId();
    }
}