package com.owsb.model.supplier;

import java.util.ArrayList;
import java.util.List;

/**
 * Supplier class representing a vendor who supplies items
 */
public class Supplier {
    private String supplierID;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private List<String> itemIDs; // Items supplied by this supplier
    
    /**
     * Constructor with essential supplier information
     */
    public Supplier(String supplierID, String name, String contactPerson, String phone) {
        this.supplierID = supplierID;
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.itemIDs = new ArrayList<>();
    }
    
    // Getters and setters...
    public String getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(String supplierID) {
        this.supplierID = supplierID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getItemIDs() {
        return itemIDs;
    }

    public void setItemIDs(List<String> itemIDs) {
        this.itemIDs = itemIDs;
    }
    
    /**
     * Associates an item with this supplier
     * @param itemID Item to be associated with supplier
     */
    public void addItem(String itemID) {
        if (!itemIDs.contains(itemID)) {
            itemIDs.add(itemID);
        }
    }
    
    /**
     * Removes an item association from this supplier
     * @param itemID Item to be disassociated
     */
    public void removeItem(String itemID) {
        itemIDs.remove(itemID);
    }
}