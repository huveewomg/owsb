package com.owsb.controller;

import com.owsb.model.Item;
import com.owsb.model.Sale;
import com.owsb.model.SaleItem;
import com.owsb.model.User;
import com.owsb.repository.ItemRepository;
import com.owsb.repository.SalesRepository;
import com.owsb.util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for sales operations
 * Demonstrates the MVC pattern by separating business logic from UI
 */
public class SalesController {
    private final SalesRepository salesRepository;
    private final ItemRepository itemRepository;
    private User currentUser;
    
    // Reference to the default profit ratio constant
    public static final double DEFAULT_PROFIT_RATIO = Constants.DEFAULT_PROFIT_RATIO;
    
    /**
     * Constructor for SalesController
     */
    public SalesController() {
        this.salesRepository = new SalesRepository();
        this.itemRepository = new ItemRepository();
    }
    
    /**
     * Set the current user
     * @param user Current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Get all sales
     * @return List of all sales
     */
    public List<Sale> getAllSales() {
        return salesRepository.findAll();
    }
    
    /**
     * Get sales for a specific date
     * @param date Date to search for
     * @return List of sales on that date
     */
    public List<Sale> getSalesByDate(Date date) {
        return salesRepository.findByDate(date);
    }
    
    /**
     * Get a sale by ID
     * @param saleId Sale ID
     * @return Sale or null if not found
     */
    public Sale getSaleById(String saleId) {
        return salesRepository.findById(saleId);
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
     * Create a new sale with multiple items
     * @param date Sale date
     * @param saleItems List of sale items
     * @param notes Optional notes
     * @return true if created successfully
     */
    public boolean createSale(Date date, List<SaleItem> saleItems, String notes) {
        // Validate inputs
        if (date == null || saleItems == null || saleItems.isEmpty()) {
            return false;
        }
        
        // Check if current user is set
        if (currentUser == null) {
            return false;
        }
        
        // Validate inventory and update stock for each item
        for (SaleItem saleItem : saleItems) {
            Item item = itemRepository.findById(saleItem.getItemID());
            if (item == null) {
                return false;
            }
            
            // Check if there's enough stock
            if (item.getCurrentStock() < saleItem.getQuantity()) {
                return false;
            }
            
            // Update the item stock
            item.setCurrentStock(item.getCurrentStock() - saleItem.getQuantity());
            itemRepository.update(item);
        }
        
        // Create a new sale
        String saleId = salesRepository.generateNewSaleID();
        Sale sale = new Sale(saleId, date, currentUser.getUserId(), notes, saleItems);
        
        // Save the sale
        return salesRepository.save(sale);
    }
    
    /**
     * Update an existing sale
     * @param saleId Sale ID
     * @param date Sale date
     * @param saleItems List of sale items
     * @param notes Optional notes
     * @return true if updated successfully
     */
    public boolean updateSale(String saleId, Date date, List<SaleItem> saleItems, String notes) {
        // Get the existing sale
        Sale existingSale = salesRepository.findById(saleId);
        if (existingSale == null) {
            return false;
        }
        
        // Restore the original stock levels for existing items
        for (SaleItem oldItem : existingSale.getItems()) {
            Item item = itemRepository.findById(oldItem.getItemID());
            if (item != null) {
                item.setCurrentStock(item.getCurrentStock() + oldItem.getQuantity());
                itemRepository.update(item);
            }
        }
        
        // Validate inventory and update stock for new items
        for (SaleItem newItem : saleItems) {
            Item item = itemRepository.findById(newItem.getItemID());
            if (item == null) {
                // Rollback stock changes?
                return false;
            }
            
            // Check if there's enough stock
            if (item.getCurrentStock() < newItem.getQuantity()) {
                // Rollback stock changes?
                return false;
            }
            
            // Update the item stock
            item.setCurrentStock(item.getCurrentStock() - newItem.getQuantity());
            itemRepository.update(item);
        }
        
        // Create an updated sale
        Sale updatedSale = new Sale(saleId, date, existingSale.getSalesManagerID(), notes, saleItems);
        
        // Update the sale
        return salesRepository.update(updatedSale);
    }
    
    /**
     * Delete a sale
     * @param saleId Sale ID
     * @return true if deleted successfully
     */
    public boolean deleteSale(String saleId) {
        // Get the sale
        Sale sale = salesRepository.findById(saleId);
        if (sale == null) {
            return false;
        }
        
        // Restore the stock levels for each item
        for (SaleItem saleItem : sale.getItems()) {
            Item item = itemRepository.findById(saleItem.getItemID());
            if (item != null) {
                item.setCurrentStock(item.getCurrentStock() + saleItem.getQuantity());
                itemRepository.update(item);
            }
        }
        
        // Delete the sale
        return salesRepository.delete(saleId);
    }
    
    /**
     * Get all items
     * @return List of all items
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    
    /**
     * Create a sale item from an item
     * @param itemId Item ID
     * @param quantity Quantity
     * @param profitRatio Profit ratio
     * @return SaleItem or null if item not found
     */
    public SaleItem createSaleItem(String itemId, int quantity, double profitRatio) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            return null;
        }
        
        return new SaleItem(
                item.getItemID(),
                item.getName(),
                quantity,
                item.getUnitPrice(),
                profitRatio
        );
    }
}