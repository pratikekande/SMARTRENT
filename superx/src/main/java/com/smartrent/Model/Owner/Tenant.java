package com.smartrent.Model.Owner;

/**
 * Represents a single tenant.
 * CORRECTED: Renamed class from 'addTenant' to 'Tenant' for clarity.
 */
public class Tenant {

    private String name;
    private long contactNumber;
    private String email;
    private String flatNumber;
    private String societyName;
    private double rentAmount;

    /**
     * A public no-argument constructor is required by Firestore for data deserialization.
     */
    public Tenant() {}

    // --- Getters and Setters ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(long contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public String getSocietyName() {
        return societyName;
    }

    public void setSocietyName(String societyName) {
        this.societyName = societyName;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
    }
}