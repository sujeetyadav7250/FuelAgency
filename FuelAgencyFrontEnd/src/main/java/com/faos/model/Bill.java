package com.faos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data  // Generates Getters, Setters, toString, equals, and hashCode
@AllArgsConstructor  // Generates a constructor with all fields
@NoArgsConstructor   // Generates a no-argument constructor
public class Bill {
    private Long billId;
    private BigDecimal price;
    private BigDecimal gst;
    private BigDecimal deliveryCharge;
    private BigDecimal CLECharge;  // Cylinder Limit Exceed Charge
    private BigDecimal totalPrice;
    private Long userId; // ✅ Added userId to match backend changes
    private Booking booking;  //  Link Bill with Booking
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
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Booking getBooking() {
		return booking;
	}
	public void setBooking(Booking booking) {
		this.booking = booking;
	}
    
}
