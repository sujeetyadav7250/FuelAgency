package com.faos.model;

import java.util.List;

import com.faos.enums.EntityStatus;

public class Supplier {

    private Long supplierId;
    private String name;
    private String contactPerson;
    private String phNo;
    private String email;
    private String address;
    private int licenseNumber;
    private EntityStatus status = EntityStatus.ACTIVE;
    private int cylinderCount;
    private List<Cylinder> cylinders;

    // Getters and Setters
    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhNo() {
        return phNo;
    }

    public void setPhNo(String phNo) {
        this.phNo = phNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(int licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public int getCylinderCount() {
        return cylinderCount;
    }

    public void setCylinderCount(int cylinderCount) {
        this.cylinderCount = cylinderCount;
    }

    public List<Cylinder> getCylinders() {
        return cylinders;
    }

    public void setCylinders(List<Cylinder> cylinders) {
        this.cylinders = cylinders;
    }
}
