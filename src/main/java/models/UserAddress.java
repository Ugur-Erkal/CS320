package models;

public class UserAddress {



        private int addressId;
        private String address;
        private String city;


        public UserAddress(int addressId, String address, String city) {
            this.addressId = addressId;
            this.address = address;
            this.city = city;
        }


        public UserAddress(String address, String city) {
            this.address = address;
            this.city = city;
        }

        public int getAddressId() { return addressId; }
        public String getAddress() { return address; }
        public String getCity() { return city; }
    }

