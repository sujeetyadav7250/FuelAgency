package com.faos.service;

import com.faos.exception.ResourceNotFoundException;
import com.faos.model.Bill;
import com.faos.repositories.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    // ✅ Get all bills
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    // ✅ Get bill by ID
    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + id));
    }

    // ✅ Update a bill
    @Transactional
    public Bill updateBill(Long id, Bill updatedBill) {
        // Reuse getBillById so we throw ResourceNotFound if missing
        Bill bill = getBillById(id);

        bill.setPrice(updatedBill.getPrice());
        bill.setGst(updatedBill.getGst());
        bill.setDeliveryCharge(updatedBill.getDeliveryCharge());
        bill.setCLECharge(updatedBill.getCLECharge());
        bill.setTotalPrice(updatedBill.getTotalPrice());

        return billRepository.save(bill);
    }

    // ✅ Delete a bill by ID
    @Transactional
    public void deleteBill(Long id) {
        if (!billRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bill with ID " + id + " not found.");
        }
        billRepository.deleteById(id);
    }
}
