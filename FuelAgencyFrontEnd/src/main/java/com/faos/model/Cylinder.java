package com.faos.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.faos.enums.EntityStatus;

import jakarta.validation.constraints.Pattern;

public class Cylinder {

    private int cylinderId;
    @Pattern(regexp = "domestic|commercial", message = "Type must be either 'domestic' or 'commercial'")
    private String type;
    private String cylinderStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lastRefillDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
//    private Date nextRefillDate;
    private Supplier supplier;
    private EntityStatus status = EntityStatus.ACTIVE;

    // Getters and Setters
    public int getCylinderId() {
        return cylinderId;
    }

    public void setCylinderId(int cylinderId) {
        this.cylinderId = cylinderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCylinderStatus() {
        return cylinderStatus;
    }

    public void setCylinderStatus(String cylinderStatus) {
        this.cylinderStatus = cylinderStatus;
    }

    public Date getLastRefillDate() {
        return lastRefillDate;
    }

    public void setLastRefillDate(Date lastRefillDate) {
        this.lastRefillDate = lastRefillDate;
    }

//    public Date getNextRefillDate() {
//        return nextRefillDate;
//    }
//
//    public void setNextRefillDate(Date nextRefillDate) {
//        this.nextRefillDate = nextRefillDate;
//    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }
}
