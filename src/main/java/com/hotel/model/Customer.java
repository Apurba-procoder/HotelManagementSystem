package com.hotel.model;

import java.util.Objects;

/**
 * Model representing a hotel guest/customer.
 */
public class Customer {

    private int customerId;
    private String fullName;
    private String phone;
    private String email;
    private String gender; // Male, Female, Other
    private String address;
    private String identityProofType; // Passport, Driver's License, National ID, Aadhaar, etc.
    private String identityProofNumber;

    public Customer() {}

    public Customer(int customerId, String fullName, String phone, String email, String gender,
                    String address, String identityProofType, String identityProofNumber) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.address = address;
        this.identityProofType = identityProofType;
        this.identityProofNumber = identityProofNumber;
    }

    public Customer(String fullName, String phone, String email, String gender,
                    String address, String identityProofType, String identityProofNumber) {
        this(0, fullName, phone, email, gender, address, identityProofType, identityProofNumber);
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdentityProofType() {
        return identityProofType;
    }

    public void setIdentityProofType(String identityProofType) {
        this.identityProofType = identityProofType;
    }

    public String getIdentityProofNumber() {
        return identityProofNumber;
    }

    public void setIdentityProofNumber(String identityProofNumber) {
        this.identityProofNumber = identityProofNumber;
    }

    @Override
    public String toString() {
        return fullName + " (ID: " + customerId + ", Ph: " + phone + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        return customerId == customer.customerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }
}
