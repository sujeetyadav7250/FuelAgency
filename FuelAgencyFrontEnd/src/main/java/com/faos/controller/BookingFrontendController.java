package com.faos.controller;

import com.faos.model.Booking;
import com.faos.model.User;
import com.faos.enums.BookingStatus;
import com.faos.enums.ConnectionStatus;
import com.faos.enums.ConnectionType;
import com.faos.enums.UserRole;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/bookings") // Base path for booking-related operations?
public class BookingFrontendController {

    private final String backendUrl = "http://localhost:8080/api"; // Backend API URL
    private final RestTemplate restTemplate = new RestTemplate(); // RestTemplate for API calls

    // ✅ Show Home Page
    @GetMapping("/")
    public String showHome() {
        return "index"; // Home page
    }
    
    // ✅ Show Login Page
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Login page
    }

    // ✅ Show Dashboard After Login
    @GetMapping("/dashboard/{role}/{userId}")
    public String showDashboard(@PathVariable Long userId, @PathVariable String role, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        return "dashboard"; // Redirects to dashboard.html
    }
    
    // User Management page (for Admin)
    @GetMapping("/admin/users/{userId}")
    public String showUserManagement(@PathVariable Long userId, Model model) {
        try {
            // Fetch the specific user by userId
            User user = restTemplate.getForObject(backendUrl + "/users/" + userId, User.class);
            
            // Check if user exists and has ADMIN role
            if (user != null && user.getRole() == UserRole.ADMIN) {
                // Fetch all users from backend
                Object[] usersArray = restTemplate.getForObject(backendUrl + "/users", Object[].class);
                List<Object> users = (usersArray != null) ? Arrays.asList(usersArray) : List.of();
                model.addAttribute("role", UserRole.ADMIN.toString());
                model.addAttribute("userId", userId);
                model.addAttribute("users", users);
                model.addAttribute("connectionTypes", ConnectionType.values());
                return "customer-management"; // Thymeleaf template for user management
            } else {
                // User not found or not an admin
                model.addAttribute("errorMessage", "You are not authorized to view this page.");
                return "error"; // Return error template
            }
        } catch (Exception e) {
            System.err.println("Error in user management: " + e.getMessage());
            model.addAttribute("errorMessage", "Unable to process your request at the moment.");
            return "error";
        }
    }
    
    @PostMapping("/admin/register-users/{userId}")
    public String saveUser(@ModelAttribute User user, @PathVariable Long userId, RedirectAttributes redirectAttributes,  Model model) {
        try {
            // Set default values if not provided
            if (user.getRegistrationDate() == null) {
                user.setRegistrationDate(LocalDate.now());
            }
            
            if (user.getConnectionStatus() == null) {
                user.setConnectionStatus(ConnectionStatus.ACTIVE);
            }
            
            if (user.getRole() == null) {
                user.setRole(UserRole.CUSTOMER);
            }

            // Ensure backend URL is correct
            String apiUrl = backendUrl + "/users";

            // Log request for debugging
            System.out.println("Sending User Data: " + user);
            System.out.println("API Endpoint: " + apiUrl);

            // Configure headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create the request entity
            HttpEntity<User> requestEntity = new HttpEntity<>(user, headers);

            // Send user registration request to backend
            ResponseEntity<User> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                User.class
            );
            
            User response = responseEntity.getBody();

            // Log response
            System.out.println("User registered successfully: " + response);

            return "redirect:/bookings/admin/users/" + userId + "?success=Registration successful";
        } catch (HttpClientErrorException ex) {
            // Handle specific error for duplicate email/phone
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                // Extract error message from response
                String errorMessage = ex.getResponseBodyAsString();
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            } else if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                redirectAttributes.addFlashAttribute("errorMessage", "Validation error: " + ex.getResponseBodyAsString());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Error registering user: " + ex.getMessage());
            }
            return "redirect:/bookings/admin/users/" + userId; // Redirect with error message
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to register user. Please try again.");
            e.printStackTrace(); // Print error in console for debugging
            return "redirect:/bookings/admin/users/" + userId; // Redirect with error message
        }
    }   

    @GetMapping("/users/update/{userId}")
    public String showUpdateUserForm(@PathVariable Long userId, Model model) {
        try {
            model.addAttribute("userId", userId);
        	model.addAttribute("role", UserRole.ADMIN.toString());
            model.addAttribute("connectionTypes", ConnectionType.values());
            return "update-customer";
        } catch (Exception e) {
            // Handle case where user is not found
            model.addAttribute("errorMessage", "User not found");
            return "error/not-found";
        }
    }
    
 // Toggle user connection status
    @PostMapping("/admin/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id, @RequestParam Long userId, @RequestParam String currentStatus, RedirectAttributes redirectAttributes) {
        try {
            // Toggle the status (if ACTIVE -> INACTIVE, if INACTIVE -> ACTIVE)
            String newStatus = "ACTIVE".equals(currentStatus) ? "INACTIVE" : "ACTIVE";

            // Call backend API to update status
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String url = backendUrl + "/users/" + userId + "/status?status=" + newStatus;
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "ACTIVE".equals(newStatus) ? "User activated successfully." : "User deactivated successfully.");
            }

            return "redirect:/bookings/admin/users/" + id;
        } catch (Exception e) {
            System.err.println("Error toggling user status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user status: " + e.getMessage());
            return "redirect:/bookings/admin/users/" + id + "?error=true";
        }
    }


 // ✅ Fetch all bookings for a user
    @GetMapping
    public String getBookings(@RequestParam Long userId, Model model) {
        try {
            // ✅ Log API request for debugging
            System.out.println("Fetching bookings for user ID: " + userId);

            // ✅ Fetch bookings from backend
            Booking[] bookingsArray = restTemplate.getForObject(backendUrl + "/bookings/user/" + userId, Booking[].class);

            // ✅ Handle empty or null responses
            List<Booking> bookings = (bookingsArray != null) ? Arrays.asList(bookingsArray) : List.of();

            // ✅ Log response
            System.out.println("Bookings retrieved: " + bookings.size());

            model.addAttribute("role", UserRole.CUSTOMER.toString());
            model.addAttribute("bookings", bookings);
            model.addAttribute("userId", userId);
            return "booking-list"; // Thymeleaf template for displaying bookings
        } catch (Exception e) {
            System.err.println("Error fetching bookings: " + e.getMessage()); // ✅ Print error in console
            model.addAttribute("errorMessage", "Unable to fetch bookings at the moment.");
            return "error"; // Redirect to error page
        }
    }


    // ✅ Fetch all bills for a user
    @GetMapping("/bills")
    public String getBills(@RequestParam Long userId, Model model) {
        try {
            Object[] billsArray = restTemplate.getForObject(backendUrl + "/bills/user/" + userId, Object[].class);
            List<Object> bills = Arrays.asList(billsArray);
            model.addAttribute("bills", bills);
            model.addAttribute("userId", userId);
            return "bill-list"; // Thymeleaf template for displaying bills
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to fetch bills at the moment.");
            return "error";
        }
    }

    // ✅ Show booking form (Requires User ID & Cylinder ID)
    @GetMapping("/new")
    public String newBookingForm(@RequestParam Long userId, @RequestParam int cylinderId, Model model) {
        Booking booking = new Booking();
        booking.setBookingDate(java.time.LocalDate.now()); // Default booking date to today
        
        // ✅ Fetch latest booking for this user
        Booking[] bookingsArray = restTemplate.getForObject(backendUrl + "/bookings/user/" + userId, Booking[].class);
        List<Booking> bookings = (bookingsArray != null) ? Arrays.asList(bookingsArray) : List.of();

        boolean restriction = false;
        if (!bookings.isEmpty()) {
            Booking lastBooking = bookings.get(bookings.size() - 1); // Get last booking
            java.time.LocalDate lastBookingDate = lastBooking.getBookingDate();
            BookingStatus bookingStatus = lastBooking.getBookingStatus(); // Assuming this returns the enum
        
            // ✅ Check booking status first
            if (bookingStatus == BookingStatus.CANCELLED) {
                restriction = false;
            } else if ((bookingStatus == BookingStatus.CONFIRMED || bookingStatus == BookingStatus.DELIVERED) 
                       && lastBookingDate.plusDays(30).isAfter(java.time.LocalDate.now())) {
                // Apply restriction only if AVAILABLE or DELIVERED AND within 30 days
                restriction = true;
            } else {
                // All other cases: different status or outside 30-day window
                restriction = false;
            }
        }
        model.addAttribute("role", UserRole.CUSTOMER.toString());
        model.addAttribute("restriction", restriction);
        model.addAttribute("booking", booking);
        model.addAttribute("userId", userId);
        model.addAttribute("cylinderId", cylinderId);
        return "booking-form"; // Show booking form
    }

    // ✅ Handle form submission to save a new booking
    @PostMapping("/save")
    public String saveBooking(@ModelAttribute Booking booking, @RequestParam Long userId, @RequestParam int cylinderId, Model model) {
        try {
            // ✅ Ensure backend URL is correct
            String apiUrl = backendUrl + "/bookings/user/" + userId + "/cylinder/" + cylinderId;

            // ✅ Log request for debugging
            System.out.println("Sending Booking Data: " + booking);
            System.out.println("API Endpoint: " + apiUrl);

            // ✅ Send booking request to backend
            Booking response = restTemplate.postForObject(apiUrl, booking, Booking.class);
            
            // ✅ Log response
            System.out.println("Booking saved successfully: " + response);

            return "redirect:/bookings?userId=" + userId; // Redirect to bookings page
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to save booking. Please try again.");
            e.printStackTrace(); // ✅ Print error in console for debugging
            return "error";
        }
    }

    // ✅ Delete a booking
    @SuppressWarnings("null")
    @GetMapping("/delete/{id}/{userId}")
    public String deleteBooking(@PathVariable Long id, @PathVariable Long userId) {
        try {
            // Changed from delete() to exchange() with HttpMethod.PUT
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
            restTemplate.exchange(
                backendUrl + "/bookings/" + id + "/cancel",
                HttpMethod.PUT,
                requestEntity,
                Booking.class
            );
            return "redirect:/reports/manage/" + userId; // Redirect after deletion
        } catch (Exception e) {
            return "redirect:/reports/manage/" + userId;
        }
    }

    @SuppressWarnings("null")
    @GetMapping("/deliver/{id}/{userId}")
    public String markBookingAsDelivered(@PathVariable Long id, @PathVariable Long userId) {
        try {
            // Changed from delete() to exchange() with HttpMethod.PUT
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
            restTemplate.exchange(
                backendUrl + "/bookings/" + id + "/deliver",
                HttpMethod.PUT,
                requestEntity,
                Booking.class
            );
            return "redirect:/reports/manage/" + userId; // Redirect after marking as delivered
        } catch (Exception e) {
            return "redirect:/reports/manage/" + userId;
        }
    }    

}
