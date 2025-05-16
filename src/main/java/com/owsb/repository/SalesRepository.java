package com.owsb.repository;

import com.owsb.model.sales.Sale;
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
 * Repository for Sale entities
 * Implements the Repository interface demonstrating polymorphism
 */
public class SalesRepository implements Repository<Sale> {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * Find all sales
     * @return List of all sales
     */
    @Override
    public List<Sale> findAll() {
        try {
            Type type = FileUtils.getListType(Sale.class);
            List<Sale> sales = FileUtils.readListFromJson(Constants.SALES_FILE, type);
            
            return sales != null ? sales : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error reading sales: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Find a sale by ID
     * @param id Sale ID
     * @return Sale or null if not found
     */
    @Override
    public Sale findById(String id) {
        List<Sale> sales = findAll();
        return sales.stream()
                .filter(sale -> sale.getSaleID().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Save a new sale
     * @param entity Sale to save
     * @return true if saved successfully
     */
    @Override
    public boolean save(Sale entity) {
        List<Sale> sales = findAll();
        
        // Check for duplicate ID
        if (sales.stream().anyMatch(sale -> sale.getSaleID().equals(entity.getSaleID()))) {
            return false;
        }
        
        sales.add(entity);
        return saveList(sales);
    }
    
    /**
     * Update an existing sale
     * @param entity Sale to update
     * @return true if updated successfully
     */
    @Override
    public boolean update(Sale entity) {
        List<Sale> sales = findAll();
        
        boolean found = false;
        for (int i = 0; i < sales.size(); i++) {
            if (sales.get(i).getSaleID().equals(entity.getSaleID())) {
                sales.set(i, entity);
                found = true;
                break;
            }
        }
        
        return found && saveList(sales);
    }
    
    /**
     * Delete a sale
     * @param id Sale ID
     * @return true if deleted successfully
     */
    @Override
    public boolean delete(String id) {
        List<Sale> sales = findAll();
        int originalSize = sales.size();
        
        sales = sales.stream()
                .filter(sale -> !sale.getSaleID().equals(id))
                .collect(Collectors.toList());
        
        return (sales.size() < originalSize) && saveList(sales);
    }
    
    /**
     * Find sales by date
     * @param date Date to search for
     * @return List of sales on that date
     */
    public List<Sale> findByDate(Date date) {
        String targetDate = dateFormat.format(date);
        return findAll().stream()
                .filter(sale -> dateFormat.format(sale.getDate()).equals(targetDate))
                .collect(Collectors.toList());
    }
    
    /**
     * Find sales by item
     * @param itemId Item ID to search for
     * @return List of sales containing that item
     */
    public List<Sale> findByItem(String itemId) {
        return findAll().stream()
                .filter(sale -> sale.getItems().stream()
                        .anyMatch(item -> item.getItemID().equals(itemId)))
                .collect(Collectors.toList());
    }
    
    /**
     * Generate a new unique sale ID
     * @return New sale ID
     */
    public String generateNewSaleID() {
        List<Sale> sales = findAll();
        
        if (sales.isEmpty()) {
            return "SL001";
        }
        
        // Find the highest sale ID number
        int maxId = sales.stream()
                .map(sale -> Integer.parseInt(sale.getSaleID().substring(2)))
                .max(Integer::compare)
                .orElse(0);
        
        // Generate the next ID
        return String.format("SL%03d", maxId + 1);
    }
    
    /**
     * Save the list of sales to the file
     * @param sales List of sales to save
     * @return true if saved successfully
     */
    private boolean saveList(List<Sale> sales) {
        try {
            FileUtils.writeListToJson(Constants.SALES_FILE, sales);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving sales: " + e.getMessage());
            return false;
        }
    }
}