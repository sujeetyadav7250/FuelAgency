package com.faos.controller;

import com.faos.model.Cylinder;
import com.faos.service.CylinderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;


@CrossOrigin(origins = "http://localhost:1000") // ✅ Allow frontend requests
@RestController
@RequestMapping("/api/cylinders")
public class CylinderController {

    @Autowired
    private CylinderService cylinderService;

    @PostMapping("/addCylinder/{supplierId}")
    public ResponseEntity<Cylinder> addCylinder(@Valid @RequestBody Cylinder cylinder,
                                                @PathVariable long supplierId) {
        return new ResponseEntity<>(cylinderService.addCylinder(cylinder, supplierId),
                HttpStatus.CREATED);
    }

    @GetMapping("/viewActiveCylinders")
    public ResponseEntity<List<Cylinder>> getAllCylinders() {
        return ResponseEntity.ok(cylinderService.getAllCylinders());
    }

    // Get a cylinder by ID
    @GetMapping("/{id}")
    public ResponseEntity<Cylinder> getCylinderById(@PathVariable int id) {
        return ResponseEntity.ok(cylinderService.getCylinderById(id));
    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteCylinder(@PathVariable int id) {
//        cylinderService.deleteCylinder(id);
//        return ResponseEntity.ok("Cylinder deleted successfully");
//    }

    @PutMapping("/{id}")
    public ResponseEntity<Cylinder> updateCylinder(@PathVariable int id,
                                                   @Valid @RequestBody Cylinder cylinder) {
        return ResponseEntity.ok(cylinderService.updateCylinder(id, cylinder));
    }

    // Refill Cylinder Endpoint with Validations
    @PutMapping("/refill/{id}")
    public ResponseEntity<Cylinder> refillCylinder(@PathVariable int id,
                                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @NotNull Date lastRefillDate) {
        Cylinder refilledCylinder = cylinderService.refillCylinder(id, lastRefillDate);
        return ResponseEntity.ok(refilledCylinder);
    }
    
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCylinder(@PathVariable("id") int id) {
        // Perform DELETE request using RestTemplate or service
        cylinderService.deleteCylinder(id); // Call the service to delete the cylinder
        return ResponseEntity.ok("Cylinder deleted successfully");
    }

    // Filter by status only
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getCylindersByStatus(@PathVariable String status) {
        List<Cylinder> cylinders = cylinderService.getCylindersByStatus(status);
        if (cylinders.isEmpty()) {
            return ResponseEntity.status(404).body("Cylinders not found with status: " + status);
        }
        return ResponseEntity.ok(cylinders);
    }

    // Filter by type only
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getCylindersByType(@PathVariable String type) {
        List<Cylinder> cylinders = cylinderService.getCylindersByType(type);
        if (cylinders.isEmpty()) {
            return ResponseEntity.status(404).body("Cylinders not found with type: " + type);
        }
        return ResponseEntity.ok(cylinders);
    }

    // Combined filter (both status and type)
    @GetMapping("/filter")
    public ResponseEntity<?> filterCylinders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {

        if (status == null && type == null) {
            return ResponseEntity.badRequest().body("At least one filter parameter (status or type) is required");
        }

        List<Cylinder> cylinders = cylinderService.filterCylinders(status, type);

        if (status != null && type != null && cylinders.isEmpty()) {
            return ResponseEntity.status(404).body(
                    "Cylinders not found with status: " + status + " and type: " + type);
        }
        else if (status != null && cylinders.isEmpty()) {
            return ResponseEntity.status(404).body("Cylinders not found with status: " + status);
        }
        else if (type != null && cylinders.isEmpty()) {
            return ResponseEntity.status(404).body("Cylinders not found with type: " + type);
        }

        return ResponseEntity.ok(cylinders);
    }
}
