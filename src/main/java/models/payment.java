package models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class payment {

    private int paymentId;
    private int userId;
    private BigDecimal amount;
    private Timestamp paidAt;

    public payment(int paymentId, int userId, BigDecimal amount, Timestamp paidAt) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.amount = amount;
        this.paidAt = paidAt;
    }

    public payment(int userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public int getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }
}
