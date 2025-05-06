package com.faos.controller;

import com.faos.exception.InvalidEntityException;
import com.faos.exception.NoActiveSuppliersException;
import com.faos.exception.NoInactiveSuppliersException;
import com.faos.model.Supplier;
import com.faos.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/viewActiveSuppliers")
    public ResponseEntity<?> getAllActiveSuppliers() {
        try {
            List<Supplier> activeSuppliers = supplierService.getAllActiveSuppliers();
            return ResponseEntity.ok(activeSuppliers);
        } catch (NoActiveSuppliersException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/viewInactiveSuppliers")
    public ResponseEntity<?> getAllInactiveSuppliers() {
        try {
            List<Supplier> inactiveSuppliers = supplierService.getAllInactiveSuppliers();
            return ResponseEntity.ok(inactiveSuppliers);
        } catch (NoInactiveSuppliersException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSupplierById(@PathVariable Long id) {
        try {
            Supplier supplier = supplierService.getSupplierById(id);
            return ResponseEntity.ok(supplier);
        } catch (InvalidEntityException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody Supplier supplier) {
        Supplier savedSupplier = supplierService.addSupplier(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSupplier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @Valid @RequestBody Supplier updatedSupplier) {
        Supplier supplier = supplierService.updateSupplier(id, updatedSupplier);
        return ResponseEntity.ok(supplier);
    }

    @PutMapping("/deactivateSupplier/{id}")
    public ResponseEntity<String> deactivateSupplier(@PathVariable Long id) {
        supplierService.deactivateSupplier(id);
        return new ResponseEntity<>("Supplier deactivated successfully!", HttpStatus.OK);
    }

    @PutMapping("/activateSupplier/{id}")
    public ResponseEntity<?> activateSupplier(@PathVariable Long id) {
        try {
            Supplier activatedSupplier = supplierService.activateSupplier(id);
            return ResponseEntity.ok(activatedSupplier);
        } catch (InvalidEntityException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
    @GetMapping("/activeSuppliers/pdf")
    public ResponseEntity<byte[]> downloadActiveSuppliersPdf() {
        try {
            byte[] pdfBytes = supplierService.getActiveSuppliersPdf();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=active_suppliers.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null); // Handle the error appropriately
        }
    }

    @GetMapping("/inactiveSuppliers/pdf")
    public ResponseEntity<byte[]> downloadInactiveSuppliersPdf() {
        try {
            byte[] pdfBytes = supplierService.getInactiveSuppliersPdf();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inactive_suppliers.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null); // Handle the error appropriately
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Supplier>> searchSuppliersByName(
            @RequestParam String name) {
        return ResponseEntity.ok(supplierService.searchSuppliersByName(name));
    }

    @GetMapping("/search/custom")
    public ResponseEntity<List<Supplier>> searchSuppliersByNameCustom(
            @RequestParam String name) {
        return ResponseEntity.ok(supplierService.searchSuppliersByNameCustom(name));
    }

    @GetMapping("/search/exact")
    public ResponseEntity<List<Supplier>> findSuppliersByExactName(
            @RequestParam String name) {
        return ResponseEntity.ok(supplierService.findSuppliersByExactName(name));
    }

    @GetMapping("/filter/status/{status}")
    public ResponseEntity<?> getSuppliersByStatus(@PathVariable String status) {
        if (!status.equalsIgnoreCase("ACTIVE") && !status.equalsIgnoreCase("INACTIVE")) {
            return ResponseEntity.badRequest().body("Invalid status. Must be either ACTIVE or INACTIVE");
        }

        List<Supplier> suppliers = supplierService.getSuppliersByStatus(status.toUpperCase());

        if (suppliers.isEmpty()) {
            return ResponseEntity.status(404).body("No suppliers found with status: " + status);
        }

        return ResponseEntity.ok(suppliers);
    }
}