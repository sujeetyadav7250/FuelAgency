package com.faos.controller;

import com.faos.model.Bill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
public class BillController {

    @Autowired
    private RestTemplate restTemplate;

    private final String backendUrl = "http://localhost:8080/api/bills";

    // Fetch all bills and pass them to Thymeleaf
    @GetMapping("/bills")
    public String getBills(Model model) {
        try {
            Bill[] billsArray = restTemplate.getForObject(backendUrl, Bill[].class);
            List<Bill> bills = Arrays.asList(billsArray);
            model.addAttribute("bills", bills);
            return "bill-list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to fetch bills at the moment.");
            return "error";  // Redirects to the error page
        }
    }

 // ✅ Fetch a single bill using billId only, and retrieve userId from it
    @GetMapping("bills/{billId}")
    public String getBillDetails(@PathVariable Long billId, Model model) {
        try {
            String apiUrl = backendUrl + "/" + billId;

            // ✅ Debugging Log
            System.out.println("Fetching bill details for bill ID: " + billId);

            Bill bill = restTemplate.getForObject(apiUrl, Bill.class);

            if (bill == null) {
                model.addAttribute("errorMessage", "Bill with ID " + billId + " not found.");
                return "error";
            }

            // ✅ Extract userId from Bill object
            Long userId = bill.getUserId();
            System.out.println("the user ID with this Bill: " + userId);

            model.addAttribute("bill", bill);
            model.addAttribute("userId", userId); // ✅ Ensure userId is passed to the view
            return "bill-details"; // Thymeleaf template for bill details

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Unable to fetch bill details.");
            return "error";
        }
    }

}
