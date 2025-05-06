package com.faos.repositories;

import com.faos.model.Booking;
import com.faos.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	
	// Add to BookingRepository.java
	@Query("SELECT b FROM Booking b WHERE b.user.userId = :userId ORDER BY b.bookingDate DESC")
	List<Booking> findMostRecentBookingsByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(b.cylinderCount), 0) FROM Booking b WHERE YEAR(b.bookingDate) = :year")
    Integer countCylindersBookedThisYear(@Param("year") int year);

    // ✅ Ensure Bill is fetched with Booking
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.bill")
    List<Booking> findAllWithBill();

    // New method to find cancelled bookings
    List<Booking> findByBookingStatus(BookingStatus bookingStatus);
    
    // ✅ Fetch all bookings for a given user ID
    List<Booking> findByUser_UserId(Long userId);

}
