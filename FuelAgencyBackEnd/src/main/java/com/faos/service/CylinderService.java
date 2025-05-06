package com.faos.service;

import com.faos.exception.InvalidEntityException;
import com.faos.model.Cylinder;
import com.faos.model.Supplier;
import com.faos.model.EntityStatus;
import com.faos.repositories.CylinderRepository;
import com.faos.repositories.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class CylinderService {

    private final CylinderRepository cylinderRepo;
    private final SupplierRepository supplierRepository;
    private final SupplierService supplierService;
    private final CylinderRepository cylinderRepository;

    @Autowired
    public CylinderService(CylinderRepository cylinderRepo,
                           SupplierRepository supplierRepository,
                           SupplierService supplierService,
                           CylinderRepository cylinderRepository) {
        this.cylinderRepo = cylinderRepo;
        this.supplierRepository = supplierRepository;
        this.supplierService = supplierService;
        this.cylinderRepository = cylinderRepository;;
    }
//
//    @Autowired
//    private CylinderRepository cylinderRepo;
//
//    @Autowired
//    private SupplierRepository supplierRepository;

    // Add a new cylinder
    public Cylinder addCylinder(Cylinder cylinder, long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new InvalidEntityException("Supplier not found"));

        cylinder.setSupplier(supplier);
        supplier.setCylinderCount(supplier.getCylinderCount() + 1);

        Cylinder savedCylinder = cylinderRepo.save(cylinder);
        supplierRepository.save(supplier);

        // Check for low inventory after adding
        supplierService.checkLowInventory(supplier);

        return savedCylinder;
    }

    // Get all cylinders
    public List<Cylinder> getAllCylinders() {
        return cylinderRepo.findAll();
    }

    // Get a cylinder by ID
    public Cylinder getCylinderById(int id) {
        return cylinderRepo.findById(id)
                .orElseThrow(() -> new InvalidEntityException("Cylinder not found"));
    }

    // Delete a cylinder by ID
    public void deleteCylinder(int id) {
        Cylinder cylinder = cylinderRepo.findById(id)
                .orElseThrow(() -> new InvalidEntityException("Cylinder not found"));

        Supplier supplier = cylinder.getSupplier();
        supplier.setCylinderCount(supplier.getCylinderCount() - 1);
        supplierRepository.save(supplier);
        cylinderRepo.delete(cylinder);

        // Check for low inventory after removal
        supplierService.checkLowInventory(supplier);
    }

    public int countCylindersByStatus(Supplier supplier, String status) {
        return cylinderRepo.countBySupplierAndCylinderStatus(supplier, status);
    }

    // Update a cylinder by ID
    public Cylinder updateCylinder(int id, Cylinder updatedCylinder) {
        return cylinderRepo.findById(id).map(cylinder -> {
            cylinder.setType(updatedCylinder.getType());
            cylinder.setCylinderStatus(updatedCylinder.getCylinderStatus());
            cylinder.setLastRefillDate(updatedCylinder.getLastRefillDate());
            return cylinderRepo.save(cylinder);
        }).orElseThrow(() -> new InvalidEntityException("Cylinder not found"));
    }

    // Refill a cylinder
    public Cylinder refillCylinder(int cylinderId, Date lastRefillDate) {
        // Fetch the cylinder
        Cylinder cylinder = cylinderRepo.findById(cylinderId)
                .orElseThrow(() -> new InvalidEntityException("Cylinder not found"));

        // Validate lastRefillDate
        if (lastRefillDate == null) {
            throw new IllegalArgumentException("Last refill date cannot be null");
        }

        // Ensure lastRefillDate is not in the future
        Date currentDate = new Date();
        if (lastRefillDate.after(currentDate)) {
            throw new IllegalArgumentException("Last refill date cannot be in the future");
        }

        // Ensure the cylinder is in a valid state for refilling
        if (!"Available".equals(cylinder.getCylinderStatus())) {
            throw new IllegalStateException("Cylinder must be 'Available' for refilling");
        }

        // Ensure the supplier is active
        Supplier supplier = cylinder.getSupplier();
        if (supplier.getStatus() != EntityStatus.ACTIVE) {
            throw new IllegalStateException("Supplier must be active for refilling");
        }

        // Update the last refill date
        cylinder.setLastRefillDate(lastRefillDate);

        // Calculate the next refill date (1 month after the last refill date)
        long oneMonthInMillis = 30L * 24 * 60 * 60 * 1000; // Approximation of 1 month in milliseconds
        Date nextRefillDate = new Date(lastRefillDate.getTime() + oneMonthInMillis);

        // Save the updated cylinder
        return cylinderRepo.save(cylinder);
    }

    // Filter methods
    public List<Cylinder> getCylindersByStatus(String status) {
        return cylinderRepository.findByCylinderStatus(status);
    }

    public List<Cylinder> getCylindersByType(String type) {
        return cylinderRepository.findByType(type);
    }

    public List<Cylinder> filterCylinders(String status, String type) {
        if (status != null && type != null) {
            return cylinderRepository.findByCylinderStatusAndType(status, type);
        } else if (status != null) {
            return cylinderRepository.findByCylinderStatus(status);
        } else {
            return cylinderRepository.findByType(type);
        }
    }

}
