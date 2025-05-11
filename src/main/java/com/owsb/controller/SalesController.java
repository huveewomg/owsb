package com.owsb.controller;

import com.owsb.model.Item;
import com.owsb.model.Sale;
import com.owsb.model.User;
import com.owsb.repository.ItemRepository;
import com.owsb.repository.SalesRepository;

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
     * Create a new sale
     * @param date Sale date
     * @param itemId Item ID
     * @param quantity Quantity sold
     * @param notes Optional notes
     * @return true if created successfully
     */
    public boolean createSale(Date date, String itemId, int quantity, String notes) {
        // Validate inputs
        if (date == null || itemId == null || itemId.isEmpty() || quantity <= 0) {
            return false;
        }
        
        // Check if current user is set
        if (currentUser == null) {
            return false;
        }
        
        // Get the item
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            return false;
        }
        
        // Check if there's enough stock
        if (item.getCurrentStock() < quantity) {
            return false;
        }
        
        // Calculate sales amount
        double salesAmount = item.getUnitPrice() * quantity;
        
        // Create a new sale
        String saleId = salesRepository.generateNewSaleID();
        Sale sale = new Sale(
                saleId,
                date,
                itemId,
                item.getName(),
                quantity,
                salesAmount,
                currentUser.getUserId(),
                notes
        );
        
        // Save the sale
        boolean saved = salesRepository.save(sale);
        
        // Update the item stock
        if (saved) {
            item.setCurrentStock(item.getCurrentStock() - quantity);
            itemRepository.update(item);
        }
        
        return saved;
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
        
        // Get the item
        Item item = itemRepository.findById(sale.getItemID());
        if (item == null) {
            return false;
        }
        
        // Delete the sale
        boolean deleted = salesRepository.delete(saleId);
        
        // Update the item stock
        if (deleted) {
            item.setCurrentStock(item.getCurrentStock() + sale.getQuantity());
            itemRepository.update(item);
        }
        
        return deleted;
    }
    
    /**
     * Get all items
     * @return List of all items
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
}