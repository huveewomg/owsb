
package com.owsb.model;

public class Item {
    private String code;
    private String name;
    private int stock;

    // Constructor, Getters, and Setters
    public Item(String code, String name, int stock) {
        this.code = code;
        this.name = name;
        this.stock = stock;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }
}
