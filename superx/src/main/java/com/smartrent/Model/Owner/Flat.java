package com.smartrent.Model.Owner;

public class Flat {

    private String societyName;
    private String flatNo;
    private String address;
    private String tenantName;
    private String tenantEmail;
    private int rent;
    private String imageUrl;
    private String ownerEmail;

    /**
     * A field to store the unique document ID from Firestore. This is crucial for
     * identifying which document to update or delete.
     */
    private String flatId;

    // --- Getters and Setters ---

    public String getSocietyName() {
        return societyName;
    }

    public void setSocietyName(String societyName) {
        this.societyName = societyName;
    }

    public String getFlatNo() {
        return flatNo;
    }

    public void setFlatNo(String flatNo) {
        this.flatNo = flatNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public int getRent() {
        return rent;
    }

    public void setRent(int rent) {
        this.rent = rent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    /**
     * Gets the unique Firestore document ID for the flat.
     * @return The flat's document ID as a String.
     */
    public String getFlatId() {
        return flatId;
    }

    /**
     * Sets the unique Firestore document ID for the flat.
     * @param flatId The document ID to store.
     */
    public void setFlatId(String flatId) {
        this.flatId = flatId;
    }
}
