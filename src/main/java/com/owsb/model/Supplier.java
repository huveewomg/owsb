package com.owsb.model;

public class Supplier {
    private String supplierID;
    private String name;
    private String contactPerson;
    private String phoneNumber;

    public Supplier(String supplierID, String name, String contactPerson, String phoneNumber) {
        this.supplierID = supplierID;
        this.name = name;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
    }

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
