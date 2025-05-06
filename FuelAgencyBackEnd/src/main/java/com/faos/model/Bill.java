package com.faos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bills")
@JsonPropertyOrder({"billId", "price", "gst", "deliveryCharge", "CLECharge", "totalPrice"})
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;

    @NotNull
    @DecimalMin(value = "0.0", message = "Price must be >= 0.")
    private BigDecimal price = BigDecimal.valueOf(1000);

    @DecimalMin(value = "0.0", message = "GST must be >= 0.")
    private BigDecimal gst;

    @DecimalMin(value = "0.0", message = "Delivery charge must be >= 0.")
    private BigDecimal deliveryCharge;

    @Column(name = "CLE_charge")
    @DecimalMin(value = "0.0", message = "CLE charge must be >= 0.")
    private BigDecimal CLECharge = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Total price must be >= 0.")
    private BigDecimal totalPrice;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    @JsonIgnoreProperties(value = {"bill"})
    private Booking booking;

    // Example calculation
    public void calculateTotalPrice(int cylinderCount, boolean exceededLimit) {
        BigDecimal calculatedTotal = price.add(gst != null ? gst : BigDecimal.ZERO)
                .add(deliveryCharge != null ? deliveryCharge : BigDecimal.ZERO);

        if (exceededLimit) {
            CLECharge = calculatedTotal.multiply(BigDecimal.valueOf(0.20));
            calculatedTotal = calculatedTotal.add(CLECharge);
        }

        this.totalPrice = calculatedTotal;
    }

	// ✅ Store userId separately
    private Long userId;

    // ✅ Automatically set `userId` when setting the booking
    public void setBooking(Booking booking) {
        this.booking = booking;
        if (booking != null && booking.getUser() != null) {
            this.userId = booking.getUser().getUserId();
        }
    }

	public Long getBillId() {
		return billId;
	}

	public void setBillId(Long billId) {
		this.billId = billId;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getGst() {
		return gst;
	}

	public void setGst(BigDecimal gst) {
		this.gst = gst;
	}

	public BigDecimal getDeliveryCharge() {
		return deliveryCharge;
	}

	public void setDeliveryCharge(BigDecimal deliveryCharge) {
		this.deliveryCharge = deliveryCharge;
	}

	public BigDecimal getCLECharge() {
		return CLECharge;
	}

	public void setCLECharge(BigDecimal cLECharge) {
		CLECharge = cLECharge;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Booking getBooking() {
		return booking;
	}
	
    public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

}

