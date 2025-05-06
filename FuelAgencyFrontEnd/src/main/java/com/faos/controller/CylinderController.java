package com.faos.controller;

import com.faos.model.Cylinder;
import com.faos.model.Supplier;

import jakarta.validation.Valid;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cylinders")
public class CylinderController {private final RestTemplate restTemplate;
private static final String BACKEND_URL = "http://localhost:8080/api/cylinders";
private static final String SUPPLIER_BACKEND_URL = "http://localhost:8080/api/suppliers/viewActiveSuppliers";
private static final String SUPPLIER_ALL_BACKEND_URL = "http://localhost:8080/api/suppliers";


public CylinderController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
}

@GetMapping("/viewAll/{userid}")
public String viewCylinders(
        @RequestParam(required = false) Integer cylinderId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String supplierName,
        @PathVariable Long userid,
        Model model) {
    model.addAttribute("userId", userid);
    try {
        String url = BACKEND_URL;

        if (cylinderId != null) {
            // Filter by ID only
            Cylinder cylinder = restTemplate.getForObject(BACKEND_URL + "/" + cylinderId, Cylinder.class);
            if (cylinder != null) {
                model.addAttribute("cylinders", List.of(cylinder));
            } else {
                model.addAttribute("No cylinder found with ID: " + cylinderId);
                model.addAttribute("cylinders", List.of());
            }
        }
        else {
            // Build filter URL
            StringBuilder filterUrl = new StringBuilder(url + "/filter?");
            if (status != null) filterUrl.append("status=").append(status).append("&");
            if (type != null) filterUrl.append("type=").append(type).append("&");

            if (filterUrl.toString().contains("=")) {
                // Remove trailing & if present
                String finalUrl = filterUrl.toString().replaceAll("&$", "");
                Cylinder[] cylindersArray = restTemplate.getForObject(finalUrl, Cylinder[].class);
                List<Cylinder> cylinders = (cylindersArray != null) ? Arrays.asList(cylindersArray) : List.of();

                // Additional filtering by supplier name if needed
                if (supplierName != null && !supplierName.isEmpty()) {
                    cylinders = cylinders.stream()
                            .filter(c -> c.getSupplier() != null &&
                                    c.getSupplier().getName().equalsIgnoreCase(supplierName))
                            .collect(Collectors.toList());
                }

                model.addAttribute("cylinders", cylinders);

                if (cylinders.isEmpty()) {
                    model.addAttribute( "No cylinders found with the selected filters");
                }
            } else {
                // No filters - get all cylinders
                Cylinder[] cylindersArray = restTemplate.getForObject(url + "/viewActiveCylinders", Cylinder[].class);
                List<Cylinder> cylinders = (cylindersArray != null) ? Arrays.asList(cylindersArray) : List.of();
                model.addAttribute("cylinders", cylinders);
            }
        }

        // Get suppliers for dropdown
        Supplier[] suppliersArray = restTemplate.getForObject(SUPPLIER_ALL_BACKEND_URL, Supplier[].class);
        List<Supplier> suppliers = (suppliersArray != null) ? Arrays.asList(suppliersArray) : List.of();
        model.addAttribute("suppliers", suppliers);

        // Add filter values back to model for form repopulation
        model.addAttribute("selectedCylinderId", cylinderId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedSupplierName", supplierName);

    } catch (Exception e) {
        model.addAttribute("Failed to load cylinders: " + e.getMessage());
        model.addAttribute("cylinders", List.of());
    }

    return "cylinderList";
}   

@GetMapping("/add/{userid}")
public String showAddCylinderForm(@PathVariable Long userid, Model model) {
    model.addAttribute("userId", userid);
    model.addAttribute("cylinder", new Cylinder());

    try {
        // Fetch active suppliers for the dropdown
        Supplier[] suppliersArray = restTemplate.getForObject(SUPPLIER_BACKEND_URL, Supplier[].class);
        List<Supplier> suppliers = (suppliersArray != null) ? Arrays.asList(suppliersArray) : List.of();
        model.addAttribute("suppliers", suppliers);

        // Add a flag to indicate if there are no active suppliers
        if (suppliers.isEmpty()) {
            model.addAttribute("noActiveSuppliers", true);
        } else {
            model.addAttribute("noActiveSuppliers", false);
        }
    } catch (Exception e) {
        // Handle exceptions (e.g., backend API failure)
        model.addAttribute("error", "Failed to fetch active suppliers: " + e.getMessage());
        model.addAttribute("noActiveSuppliers", true);
    }

    return "addCylinder";
}

@PostMapping("/add/{userid}")
public String addCylinder(@RequestParam("supplierId") long supplierId, 
                          @Valid @ModelAttribute("cylinder") Cylinder cylinder, 
                          BindingResult result, @PathVariable Long userid, Model model) {
    model.addAttribute("userId", userid);                        
    if (result.hasErrors()) {
        // Reload the supplier list in case of validation failure
        Supplier[] suppliersArray = restTemplate.getForObject(SUPPLIER_BACKEND_URL, Supplier[].class);
        List<Supplier> suppliers = (suppliersArray != null) ? Arrays.asList(suppliersArray) : List.of();
        model.addAttribute("suppliers", suppliers);
        return "addCylinder";    
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Cylinder> request = new HttpEntity<>(cylinder, headers);
    restTemplate.postForObject(BACKEND_URL + "/addCylinder/" + supplierId, request, Cylinder.class);

    return "redirect:/cylinders/viewAll/" + userid; // Redirect to view all cylinders after adding
}

@GetMapping("/edit/{id}/{userid}")
public String showEditCylinderForm(@PathVariable("id") int id, @PathVariable Long userid, Model model) {
    // Changed the URL to match the correct backend mapping
    Cylinder cylinder = restTemplate.getForObject(BACKEND_URL + "/" + id, Cylinder.class);
    model.addAttribute("cylinder", cylinder);

    // Fetch active suppliers to allow changing supplier
    Supplier[] suppliersArray = restTemplate.getForObject(SUPPLIER_BACKEND_URL, Supplier[].class);
    List<Supplier> suppliers = (suppliersArray != null) ? Arrays.asList(suppliersArray) : List.of();
    model.addAttribute("suppliers", suppliers);
    model.addAttribute("userId", userid);

    return "editCylinder";
}

@PostMapping("/update/{id}/{userid}")
public String updateCylinder(@PathVariable("id") int id, 
                             @Valid @ModelAttribute("cylinder") Cylinder cylinder, 
                             BindingResult result, @PathVariable Long userid, Model model) {
    model.addAttribute("userId", userid);                            
    if (result.hasErrors()) {
        // Reload suppliers if there's an error
        Supplier[] suppliersArray = restTemplate.getForObject(SUPPLIER_BACKEND_URL, Supplier[].class);
        List<Supplier> suppliers = (suppliersArray != null) ? Arrays.asList(suppliersArray) : List.of();
        model.addAttribute("suppliers", suppliers);

        return "editCylinder";
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Cylinder> request = new HttpEntity<>(cylinder, headers);
    restTemplate.put(BACKEND_URL + "/" + id, request);

    return "redirect:/cylinders/viewAll/" + userid; // Redirect to view all cylinders after update
}

@PostMapping("/delete/{id}")
public String deleteCylinder(@PathVariable("id") int id, @RequestParam(required = false) Long userId ) {
    // Perform DELETE request using RestTemplate
    restTemplate.delete(BACKEND_URL + "/delete/" + id);
    return "redirect:/cylinders/viewAll/" + userId; // Redirect back to view all cylinders after delete
}
}
