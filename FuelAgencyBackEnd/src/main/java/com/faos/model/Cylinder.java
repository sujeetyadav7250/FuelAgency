package com.faos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cylinders")
@SequenceGenerator(name = "cylinder_seq", sequenceName = "cylinder_sequence", initialValue = 1000, allocationSize = 1)
public class Cylinder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cylinderId;

    @NotBlank(message = "Type is mandatory")
    @Pattern(regexp = "domestic|commercial", message = "Type must be either 'domestic' or 'commercial'")
    private String type;

    @NotBlank(message = "Status is mandatory")
    @Pattern(regexp = "Available|Booked|Out of Stock", message = "Status must be 'Available', 'Booked', or 'Out of Stock'")
    private String cylinderStatus;

    @NotNull(message = "Last refill date is mandatory")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lastRefillDate;


    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    @JsonIgnoreProperties("cylinders")
    private Supplier supplier;


    // Getters and Setters
    public int getCylinderId() { return cylinderId; }
    public void setCylinderId(int cylinderId) { this.cylinderId = cylinderId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCylinderStatus() { return cylinderStatus; }
    public void setCylinderStatus(String cylinderStatus) { this.cylinderStatus = cylinderStatus; }
    public Date getLastRefillDate() { return lastRefillDate; }
    public void setLastRefillDate(Date lastRefillDate) { this.lastRefillDate = lastRefillDate; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
}