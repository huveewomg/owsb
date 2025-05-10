package com.owsb.dto;


/**
 * DTO for Item - used for JSON serialization
 * Demonstrates separation of concerns between domain models and data transfer
 */
public class ItemDTO {
    public String itemID;
    public String name;
    public String description;
    public double unitPrice;
    public String category;
    public String supplierID;
    public String dateAdded;
    public int currentStock;
    public int minimumStock;
    public int maximumStock;
    public String lastUpdated;
}

