package com.smartrent.Model.Tenant;

/**
 * Model class (POJO) to represent a Tenant's profile data.
 * This allows for easy conversion from a Firestore document to a Java object.
 */
public class TenantProfileModel {
    private String name;
    private String email;
    private String phone;
    private String address;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}