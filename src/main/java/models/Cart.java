package models;


import java.sql.Timestamp;

public class Cart {

    private int cartId;
    private String status;      // Pending / Accepted / Delivered
    private Timestamp acceptedAt;

    // DB'den okurken
    public Cart(int cartId, String status, Timestamp acceptedAt) {
        this.cartId = cartId;
        this.status = status;
        this.acceptedAt = acceptedAt;
    }


    public Cart(String status) {
        this.status = status;
    }

    public int getCartId() { return cartId; }
    public String getStatus() { return status; }
    public Timestamp getAcceptedAt() { return acceptedAt; }
}



