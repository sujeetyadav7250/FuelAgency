package com.faos.controller;

import com.faos.model.Supplier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private final RestTemplate restTemplate;
    private static final String BACKEND_URL = "http://localhost:8080/api/suppliers";

    public SupplierController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/viewAll/{userid}")
    public String viewSuppliers(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @PathVariable Long userid,
            Model model) {

        try {
            String url = BACKEND_URL;

            if (id != null) {
                Supplier supplier = restTemplate.getForObject(BACKEND_URL + "/" + id, Supplier.class);
                if (supplier != null) {
                    model.addAttribute("suppliers", List.of(supplier));
                } else {
                    model.addAttribute("error", "No supplier found with ID: " + id);
                    model.addAttribute("suppliers", List.of());
                }
            }
            else if (name != null && !name.isEmpty()) {
                url += "/search?name=" + name;
                Supplier[] suppliersArray = restTemplate.getForObject(url, Supplier[].class);
                List<Supplier> suppliers = (suppliersArray != null) ? Arrays.asList(suppliersArray) : List.of();
                model.addAttribute("suppliers", suppliers);

                if (suppliers.isEmpty()) {
                    model.addAttribute("error", "No suppliers found with name: " + name);
                }
            }
            else if (status != null && !status.isEmpty()) {
                url += "/filter/status/" + status.toUpperCase();
                Supplier[] suppliersArray = restTemplate.getForObject(url, Supplier[].class);
                List<Supplier> suppliers = (suppliersArray != null) ? Arrays.asList(suppliersArray) : List.of();
                model.addAttribute("suppliers", suppliers);

                if (suppliers.isEmpty()) {
                    model.addAttribute("error", "No suppliers found with status: " + status);
                }
            }
            else {
                Supplier[] suppliersArray = restTemplate.getForObject(url, Supplier[].class);
                List<Supplier> suppliers = (suppliersArray != null) ? Arrays.asList(suppliersArray) : List.of();
                model.addAttribute("suppliers", suppliers);
            }

            model.addAttribute("selectedId", id);
            model.addAttribute("selectedName", name);
            model.addAttribute("selectedStatus", status);

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load suppliers: " + e.getMessage());
            model.addAttribute("suppliers", List.of());
        }
        model.addAttribute("userId", userid);
        return "supplierList";
    }

    @GetMapping("/getpdfActiveSupplier/{userid}")
    public String getActiveSupplierPdf(@PathVariable Long userid, Model model) {
        restTemplate.getForObject(BACKEND_URL + "/activeSuppliers/pdf", Supplier[].class);
        return "redirect:/suppliers/viewAll/" + userid;
    }

    @GetMapping("/add/{userid}")
    public String showAddSupplierForm(@PathVariable Long userid, Model model) {
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("userId", userid);
        return "addSupplier";
    }

    @PostMapping("/add/{userid}")
    public String addSupplier(@PathVariable Long userid, @Valid @ModelAttribute("supplier") Supplier supplier,
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("userId", userid);
            return "addSupplier";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Supplier> request = new HttpEntity<>(supplier, headers);
            restTemplate.postForObject(BACKEND_URL, request, Supplier.class);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add supplier: " + e.getMessage());
            model.addAttribute("userId", userid);
            return "addSupplier";
        }
        return "redirect:/suppliers/viewAll/" + userid;
    }

    @GetMapping("/edit/{id}/{userid}")
    public String showEditSupplierForm(@PathVariable Long userid, @PathVariable("id") Long id, Model model) {
        try {
            Supplier supplier = restTemplate.getForObject(BACKEND_URL + "/" + id, Supplier.class);
            model.addAttribute("supplier", supplier);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load supplier details: " + e.getMessage());
            return "redirect:/suppliers/viewAll/" + userid;
        }
        model.addAttribute("userId", userid);
        return "editSupplier";
    }

    @PostMapping("/update/{id}/{userid}")
    public String updateSupplier(@PathVariable Long userid, @PathVariable("id") Long id,
                                 @Valid @ModelAttribute("supplier") Supplier supplier,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("userId", userid);
            return "editSupplier";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Supplier> request = new HttpEntity<>(supplier, headers);
            restTemplate.put(BACKEND_URL + "/" + id, request);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update supplier: " + e.getMessage());
            model.addAttribute("userId", userid);
            return "editSupplier";
        }
        return "redirect:/suppliers/viewAll/" + userid;
    }

    @GetMapping("/deactivate/{id}/{userid}")
    public String deactivateSupplier(@PathVariable Long userid, @PathVariable("id") Long id, Model model) {
        try {
            restTemplate.put(BACKEND_URL + "/deactivateSupplier/" + id, null);
            model.addAttribute("success", "Supplier deactivated successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to deactivate supplier: " + e.getMessage());
        }
        return "redirect:/suppliers/viewAll/" + userid;
    }

    @GetMapping("/activate/{id}/{userid}")
    public String activateSupplier(@PathVariable Long userid, @PathVariable("id") Long id, Model model) {
        try {
            restTemplate.put(BACKEND_URL + "/activateSupplier/" + id, null);
            model.addAttribute("success", "Supplier activated successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to activate supplier: " + e.getMessage());
        }
        return "redirect:/suppliers/viewAll/" + userid;
    }
}