package models;



    public class Restaurant {

        private int restaurantId;
        private String restaurantName;
        private String address;
        private String city;
        private String cuisineType;

        public Restaurant(int restaurantId, String restaurantName, String address, String city, String cuisineType) {
            this.restaurantId = restaurantId;
            this.restaurantName = restaurantName;
            this.address = address;
            this.city = city;
            this.cuisineType = cuisineType;
        }

        public Restaurant(String restaurantName, String address, String city, String cuisineType) {
            this.restaurantName = restaurantName;
            this.address = address;
            this.city = city;
            this.cuisineType = cuisineType;
        }

        public int getRestaurantId() { return restaurantId; }
        public String getRestaurantName() { return restaurantName; }
        public String getAddress() { return address; }
        public String getCity() { return city; }
        public String getCuisineType() { return cuisineType; }
    }


