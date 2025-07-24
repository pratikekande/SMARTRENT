package com.smartrent.Model.Tenant;

import com.google.cloud.Timestamp;

/**
 * Represents the data for a UPI payment transaction.
 * It holds all relevant details for a payment record.
 */
public class PaymentData {

    private double rentAmount;
    private Timestamp paymentDate;
    private String upiId;
    private String tenantName;
    private String tenantEmail; // NEW: Field for the tenant's email

    public PaymentData() {}

    // --- Getters and Setters for all fields ---

    public double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
    }

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    /**
     * NEW: Gets the email of the tenant making the payment.
     * @return The tenant's email string.
     */
    public String getTenantEmail() {
        return tenantEmail;
    }

    /**
     * NEW: Sets the email of the tenant making the payment.
     * @param tenantEmail The tenant's email string to set.
     */
    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }
}
