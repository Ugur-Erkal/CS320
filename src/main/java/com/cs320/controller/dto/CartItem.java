package com.cs320.controller.dto;

public class CartItem {

    private int itemId;
    private String itemName;
    private double price;
    private int quantity;

    public CartItem(int itemId, String itemName, double price, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity() {
        this.quantity++;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}
