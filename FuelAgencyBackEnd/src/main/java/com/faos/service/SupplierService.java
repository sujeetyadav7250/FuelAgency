package com.faos.service;

import com.faos.exception.InvalidEntityException;
import com.faos.exception.NoActiveSuppliersException;
import com.faos.exception.NoInactiveSuppliersException;
import com.faos.model.EntityStatus;
import com.faos.model.Supplier;
import com.faos.repositories.CylinderRepository;
import com.faos.repositories.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SupplierService {

    private static final int LOW_INVENTORY_THRESHOLD = 5;

    private final SupplierRepository supplierRepository;
    private final EmailService emailService;
    private final PdfService pdfService;
    private final CylinderRepository cylinderRepository; // Add this instead of CylinderService

    @Autowired
    public SupplierService(SupplierRepository supplierRepository,
                           EmailService emailService,
                           PdfService pdfService,
                           CylinderRepository cylinderRepository) {
        this.supplierRepository = supplierRepository;
        this.emailService = emailService;
        this.pdfService = pdfService;
        this.cylinderRepository = cylinderRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public List<Supplier> getAllActiveSuppliers() {
        List<Supplier> activeSuppliers = supplierRepository.findAllByStatus(EntityStatus.ACTIVE);
        if (activeSuppliers.isEmpty()) {
            throw new NoActiveSuppliersException("There are no active suppliers");
        }
        return activeSuppliers;
    }

    public List<Supplier> getAllInactiveSuppliers() {
        List<Supplier> inactiveSuppliers = supplierRepository.findAllByStatus(EntityStatus.INACTIVE);
        if (inactiveSuppliers.isEmpty()) {
            throw new NoInactiveSuppliersException("There are no inactive suppliers");
        }
        return inactiveSuppliers;
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new InvalidEntityException("Supplier with ID " + id + " not found"));
    }

    public Supplier addSupplier(Supplier supplier) {
        // Status is automatically set to ACTIVE by the model
        Supplier savedSupplier = supplierRepository.save(supplier);
        sendWelcomeEmail(savedSupplier);
        return savedSupplier;
    }

    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {
        return supplierRepository.findById(id).map(supplier -> {
            String oldEmail = supplier.getEmail();

            updateSupplierFields(supplier, updatedSupplier);
            Supplier savedSupplier = supplierRepository.save(supplier);

            sendUpdateNotification(savedSupplier, oldEmail);
            return savedSupplier;
        }).orElseThrow(() -> new InvalidEntityException("Supplier with ID " + id + " not found"));
    }

    public void deactivateSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new InvalidEntityException("Supplier with ID " + id + " not found"));

        supplier.setStatus(EntityStatus.INACTIVE);
        supplierRepository.save(supplier);
        sendDeactivationEmail(supplier);
    }

    public Supplier activateSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new InvalidEntityException("Supplier with ID " + id + " not found"));

        if (supplier.getStatus() == EntityStatus.INACTIVE) {
            supplier.setStatus(EntityStatus.ACTIVE);
            supplierRepository.save(supplier);
            sendActivationEmail(supplier);
            return supplier;
        }
        throw new InvalidEntityException("Supplier with ID " + id + " is already active");
    }

    public byte[] getActiveSuppliersPdf() throws IOException {
        List<Supplier> activeSuppliers = getAllActiveSuppliers();
        return pdfService.generateSuppliersPdf(activeSuppliers, "Active Suppliers Report");
    }

    public byte[] getInactiveSuppliersPdf() throws IOException {
        List<Supplier> inactiveSuppliers = getAllInactiveSuppliers();
        return pdfService.generateSuppliersPdf(inactiveSuppliers, "Inactive Suppliers Report");
    }

    // ============ PRIVATE HELPER METHODS ============

    private void updateSupplierFields(Supplier supplier, Supplier updatedSupplier) {
        supplier.setName(updatedSupplier.getName());
        supplier.setContactPerson(updatedSupplier.getContactPerson());
        supplier.setPhNo(updatedSupplier.getPhNo());
        supplier.setEmail(updatedSupplier.getEmail());
        supplier.setAddress(updatedSupplier.getAddress());
        supplier.setLicenseNumber(updatedSupplier.getLicenseNumber());
    }

    private void sendWelcomeEmail(Supplier supplier) {
        String subject = "Welcome to Fuel Pro Management System - " + supplier.getName();
        String text = String.format(
                "Dear %s,%n%n" +
                        "Your account has been successfully created. Below are your account details:%n%n" +
                        "Supplier ID: %d%n" +
                        "Supplier Name: %s%n" +
                        "Mobile Number: %s%n" +
                        "Email: %s%n" +
                        "License Number: %d%n" +
                        "Status: ACTIVE%n%n" +
                        "Thank you for joining Fuel Pro Management System.%n%n" +
                        "Best regards,%n" +
                        "Fuel Pro Management System",
                supplier.getName(),
                supplier.getSupplierId(),
                supplier.getName(),
                supplier.getPhNo(),
                supplier.getEmail(),
                supplier.getLicenseNumber()
        );
        emailService.sendEmail(supplier.getEmail(), subject, text);
    }

    private void sendUpdateNotification(Supplier supplier, String oldEmail) {
        // Email to new address
        String subject = "Account Details Updated - " + supplier.getName();
        String text = String.format(
                "Dear %s,%n%n" +
                        "Your account details have been updated. Below are your updated details:%n%n" +
                        "Supplier ID: %d%n" +
                        "Supplier Name: %s%n" +
                        "Contact Person: %s%n" +
                        "Mobile Number: %s%n" +
                        "Email: %s%n" +
                        "Address: %s%n" +
                        "License Number: %d%n" +
                        "Status: %s%n%n" +
                        "If you did not make these changes, please contact our support team immediately.%n%n" +
                        "Best regards,%n" +
                        "Fuel Pro Management System",
                supplier.getName(),
                supplier.getSupplierId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getPhNo(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getLicenseNumber(),
                supplier.getStatus()
        );
        emailService.sendEmail(supplier.getEmail(), subject, text);

        // Additional notification to old email if it was changed
        if (!oldEmail.equals(supplier.getEmail())) {
            String oldEmailSubject = "Email Address Updated - " + supplier.getName();
            String oldEmailText = String.format(
                    "Dear %s,%n%n" +
                            "Your email address has been updated from %s to %s.%n%n" +
                            "If you did not make this change, please contact our support team immediately.%n%n" +
                            "Best regards,%n" +
                            "Fuel Pro Management System",
                    supplier.getName(),
                    oldEmail,
                    supplier.getEmail()
            );
            emailService.sendEmail(oldEmail, oldEmailSubject, oldEmailText);
        }
    }

    private void sendDeactivationEmail(Supplier supplier) {
        String subject = "Account Deactivated - " + supplier.getName();
        String text = String.format(
                "Dear %s,%n%n" +
                        "Your account has been deactivated. Below are your account details:%n%n" +
                        "Supplier ID: %d%n" +
                        "Supplier Name: %s%n" +
                        "Mobile Number: %s%n" +
                        "Email: %s%n" +
                        "License Number: %d%n" +
                        "Status: INACTIVE%n%n" +
                        "If you have any questions, please contact our support team at support@fuelpromanagement.com.%n%n" +
                        "Best regards,%n" +
                        "Fuel Pro Management System",
                supplier.getName(),
                supplier.getSupplierId(),
                supplier.getName(),
                supplier.getPhNo(),
                supplier.getEmail(),
                supplier.getLicenseNumber()
        );
        emailService.sendEmail(supplier.getEmail(), subject, text);
    }

    private void sendActivationEmail(Supplier supplier) {
        String subject = "Account Activated - " + supplier.getName();
        String text = String.format(
                "Dear %s,%n%n" +
                        "Your account has been activated. Below are your account details:%n%n" +
                        "Supplier ID: %d%n" +
                        "Supplier Name: %s%n" +
                        "Mobile Number: %s%n" +
                        "Email: %s%n" +
                        "License Number: %d%n" +
                        "Status: ACTIVE%n%n" +
                        "Thank you for being a valued partner of Gas Management System.%n%n" +
                        "Best regards,%n" +
                        "Fuel Pro Management System",
                supplier.getName(),
                supplier.getSupplierId(),
                supplier.getName(),
                supplier.getPhNo(),
                supplier.getEmail(),
                supplier.getLicenseNumber()
        );
        emailService.sendEmail(supplier.getEmail(), subject, text);
    }

    // Daily report at 10 AM
    @Scheduled(cron = "0 0 10 * * ?") // Runs every day at 10:00 AM
    public void sendDailyCylinderReports() {
        List<Supplier> suppliers = supplierRepository.findAll();
        for (Supplier supplier : suppliers) {
            sendDailyReport(supplier);
            checkLowInventory(supplier); // Also check inventory during daily report
        }
    }

    public void checkLowInventory(Supplier supplier) {
        if (supplier.getCylinderCount() < LOW_INVENTORY_THRESHOLD) {
            sendLowInventoryAlert(supplier);
        }
    }

    private void sendDailyReport(Supplier supplier) {
        String subject = "Daily Cylinder Report - " + supplier.getName();
        String text = String.format(
                "Dear %s,%n%n" +
                        "Daily Cylinder Status Report:%n%n" +
                        "Total Cylinders: %d%n" +
                        "Available: %d%n" +
                        "Booked: %d%n" +
                        "Out of Stock: %d%n%n" +
                        "Best regards,%n Fuel Pro Management System",
                supplier.getName(),
                supplier.getCylinderCount(),
                cylinderRepository.countBySupplierAndCylinderStatus(supplier, "Available"),
                cylinderRepository.countBySupplierAndCylinderStatus(supplier, "Booked"),
                cylinderRepository.countBySupplierAndCylinderStatus(supplier, "Out of Stock")
        );
        emailService.sendEmail(supplier.getEmail(), subject, text);
    }

    private void sendLowInventoryAlert(Supplier supplier) {
        String subject = "URGENT: Low Cylinder Inventory Alert";
        String text = String.format(
                "Dear %s,%n%n" +
                        "Your cylinder inventory is below the threshold!%n%n" +
                        "Current Inventory: %d%n" +
                        "Threshold: %d%n%n" +
                        "Please restock immediately.%n%n" +
                        "Best regards,%n Fuel Pro Management System",
                supplier.getName(),
                supplier.getCylinderCount(),
                LOW_INVENTORY_THRESHOLD
        );
        emailService.sendEmail(supplier.getEmail(), subject, text);
    }

    public List<Supplier> searchSuppliersByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Supplier> searchSuppliersByNameCustom(String name) {
        return supplierRepository.searchByName(name);
    }

    public List<Supplier> findSuppliersByExactName(String name) {
        return supplierRepository.findByNameExactIgnoreCase(name);
    }

    public List<Supplier> getSuppliersByStatus(String status) {
        return supplierRepository.findByStatus(EntityStatus.valueOf(status));
    }


}
