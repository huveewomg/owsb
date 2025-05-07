package com.owsb.model;

public class Supplier {
    private String code;
    private String name;
    private String contact;

    // No-argument constructor
    public Supplier() {}

    // Constructor with parameters
    public Supplier(String code, String name, String contact) {
        this.code = code;
        this.name = name;
        this.contact = contact;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
