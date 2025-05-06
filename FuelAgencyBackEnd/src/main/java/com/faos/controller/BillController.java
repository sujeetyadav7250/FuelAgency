package com.faos.controller;

import com.faos.model.Bill;
import com.faos.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;


@CrossOrigin(origins = "http://localhost:1000") // ✅ Allow frontend requests
@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;

    // ✅ Get all bills
    @GetMapping
    public ResponseEntity<List<Bill>> getAllBills() {
        List<Bill> bills = billService.getAllBills();
        return ResponseEntity.ok(bills);
    }

    // ✅ Get bill by ID
    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        Bill bill = billService.getBillById(id);
        return ResponseEntity.ok(bill);
    }
    
    // ✅ Update an existing bill
    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBill(@PathVariable Long id,
                                           @Valid @RequestBody Bill updatedBill) {
        Bill savedBill = billService.updateBill(id, updatedBill);
        return ResponseEntity.ok(savedBill);
    }

    // ✅ Delete a bill by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok("Bill deleted successfully!");
    }
}
