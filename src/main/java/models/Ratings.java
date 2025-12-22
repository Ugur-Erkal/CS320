package models;
import java.sql.Timestamp;
public class Ratings {

        private int ratingId;
        private int rating;
        private Timestamp ratingDate;     // DATETIME
        private String comment;           // nullable
        private int cartId;               // UNIQUE


        public Ratings(int ratingId, int rating, Timestamp ratingDate, String comment, int cartId) {
            this.ratingId = ratingId;
            this.rating = rating;
            this.ratingDate = ratingDate;
            this.comment = comment;
            this.cartId = cartId;
        }


        public Ratings(int rating, Timestamp ratingDate, String comment, int cartId) {
            this.rating = rating;
            this.ratingDate = ratingDate;
            this.comment = comment;
            this.cartId = cartId;
        }

        public int getRatingId() { return ratingId; }
        public int getRating() { return rating; }
        public Timestamp getRatingDate() { return ratingDate; }
        public String getComment() { return comment; }
        public int getCartId() { return cartId; }
    }

