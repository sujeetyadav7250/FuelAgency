package com.faos.repositories;

import com.faos.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
	
	// ✅ Fetch Bill by billId only
    @Query("SELECT b FROM Bill b WHERE b.billId = :billId")
    Optional<Bill> findByBillId(@Param("billId") Long billId);

    @Query("SELECT b FROM Bill b JOIN FETCH b.booking")
    List<Bill> findAllWithBooking();

    @Query("SELECT b FROM Bill b JOIN FETCH b.booking WHERE b.billId = :billId")
    Optional<Bill> findByIdWithBooking(@Param("billId") Long billId);

}
