package com.owsb.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.owsb.dto.ItemDTO;
import com.owsb.model.Item;
import com.owsb.util.Constants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Repository pattern for Item entities
 * Handles persistence to/from JSON files
 * Demonstrates Single Responsibility Principle by focusing only on data access
 */
public class ItemRepository implements Repository<Item> {
    private final Gson gson;
    private final String filePath;
    
    /**
     * Constructor initializes Gson serializer and file path
     */
    public ItemRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.filePath = Constants.ITEM_FILE; // Combined items and inventory
    }
    
    @Override
    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        
        try {
            List<ItemDTO> itemDTOs = readItemsFromFile();
            
            for (ItemDTO dto : itemDTOs) {
                items.add(convertToItem(dto));
            }
        } catch (IOException e) {
            System.err.println("Error reading items: " + e.getMessage());
        }
        
        return items;
    }
    
    @Override
    public Item findById(String id) {
        try {
            List<ItemDTO> itemDTOs = readItemsFromFile();
            
            for (ItemDTO dto : itemDTOs) {
                if (dto.itemID.equals(id)) {
                    return convertToItem(dto);
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding item: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public boolean save(Item item) {
        try {
            List<ItemDTO> itemDTOs = readItemsFromFile();
            
            // Check for duplicates
            for (ItemDTO dto : itemDTOs) {
                if (dto.itemID.equals(item.getItemID())) {
                    return false; // Item already exists
                }
            }
            
            // Add new item
            itemDTOs.add(convertToDTO(item));
            
            // Save to file
            writeItemsToFile(itemDTOs);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving item: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean update(Item item) {
        try {
            List<ItemDTO> itemDTOs = readItemsFromFile();
            boolean updated = false;
            
            // Find and update existing item
            for (int i = 0; i < itemDTOs.size(); i++) {
                if (itemDTOs.get(i).itemID.equals(item.getItemID())) {
                    itemDTOs.set(i, convertToDTO(item));
                    updated = true;
                    break;
                }
            }
            
            if (updated) {
                writeItemsToFile(itemDTOs);
                return true;
            }
            return false; // Item not found
        } catch (IOException e) {
            System.err.println("Error updating item: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(String id) {
        try {
            List<ItemDTO> itemDTOs = readItemsFromFile();
            boolean removed = false;
            
            // Find and remove item
            for (int i = 0; i < itemDTOs.size(); i++) {
                if (itemDTOs.get(i).itemID.equals(id)) {
                    itemDTOs.remove(i);
                    removed = true;
                    break;
                }
            }
            
            if (removed) {
                writeItemsToFile(itemDTOs);
                return true;
            }
            return false; // Item not found
        } catch (IOException e) {
            System.err.println("Error deleting item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find items by supplier ID
     * Demonstrates abstraction by hiding implementation details
     * @param supplierID Supplier ID to search for
     * @return List of items from that supplier
     */
    public List<Item> findBySupplier(String supplierID) {
        List<Item> items = new ArrayList<>();
        
        try {
            List<ItemDTO> itemDTOs = readItemsFromFile();
            
            for (ItemDTO dto : itemDTOs) {
                if (dto.supplierID.equals(supplierID)) {
                    items.add(convertToItem(dto));
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding items by supplier: " + e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Find items with stock below minimum levels
     * @return List of items that need reordering
     */
    public List<Item> findLowStockItems() {
        List<Item> items = new ArrayList<>();
        
        try {
            List<ItemDTO> itemDTOs = readItemsFromFile();
            
            for (ItemDTO dto : itemDTOs) {
                if (dto.currentStock <= dto.minimumStock) {
                    items.add(convertToItem(dto));
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding low stock items: " + e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Update stock level of an item
     * @param itemID Item ID to update
     * @param quantity Quantity change (positive for additions, negative for deductions)
     * @return true if successful, false otherwise
     */
    public boolean updateStock(String itemID, int quantity) {
        Item item = findById(itemID);
        
        if (item != null) {
            item.updateStock(quantity);
            return update(item);
        }
        
        return false;
    }
    
    /**
     * Generate a new unique item ID
     * @return Next available item ID
     */
    public String generateItemId() {
        List<Item> items = findAll();
        
        // Find the highest existing ID
        int highestId = 0;
        for (Item item : items) {
            String id = item.getItemID();
            if (id.startsWith("IT")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > highestId) {
                        highestId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        // Generate next ID
        return String.format("IT%03d", highestId + 1);
    }
    
    /**
     * Read items from JSON file
     * @return List of item DTOs
     * @throws IOException if file error occurs
     */
    private List<ItemDTO> readItemsFromFile() throws IOException {
        File file = new File(filePath);
        
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        FileReader reader = new FileReader(file);
        Type listType = new TypeToken<ArrayList<ItemDTO>>(){}.getType();
        List<ItemDTO> itemDTOs = gson.fromJson(reader, listType);
        reader.close();
        
        // Handle null case when file is empty or invalid
        if (itemDTOs == null) {
            return new ArrayList<>();
        }
        
        return itemDTOs;
    }
    
    /**
     * Write items to JSON file
     * @param itemDTOs List of item DTOs to write
     * @throws IOException if file error occurs
     */
    private void writeItemsToFile(List<ItemDTO> itemDTOs) throws IOException {
        File file = new File(filePath);
        
        // Create directory if it doesn't exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        FileWriter writer = new FileWriter(file);
        gson.toJson(itemDTOs, writer);
        writer.flush();
        writer.close();
    }
    
    /**
     * Convert DTO to domain entity
     * @param dto Data Transfer Object
     * @return Domain Entity
     */
    private Item convertToItem(ItemDTO dto) {
        Item item = new Item(
            dto.itemID,
            dto.name,
            dto.description,
            dto.unitPrice,
            dto.category,
            dto.supplierID
        );
        
        // Set inventory fields
        item.setCurrentStock(dto.currentStock);
        item.setMinimumStock(dto.minimumStock);
        item.setMaximumStock(dto.maximumStock);
        item.setLastUpdated(dto.lastUpdated);
        item.setDateAdded(dto.dateAdded);
        
        return item;
    }
    
    /**
     * Convert domain entity to DTO
     * @param item Domain Entity
     * @return Data Transfer Object
     */
    private ItemDTO convertToDTO(Item item) {
        ItemDTO dto = new ItemDTO();
        
        dto.itemID = item.getItemID();
        dto.name = item.getName();
        dto.description = item.getDescription();
        dto.unitPrice = item.getUnitPrice();
        dto.category = item.getCategory();
        dto.supplierID = item.getSupplierID();
        dto.dateAdded = item.getDateAdded();
        
        // Inventory fields
        dto.currentStock = item.getCurrentStock();
        dto.minimumStock = item.getMinimumStock();
        dto.maximumStock = item.getMaximumStock();
        dto.lastUpdated = item.getLastUpdated();
        
        return dto;
    }
}