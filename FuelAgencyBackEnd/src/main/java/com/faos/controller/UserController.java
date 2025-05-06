package com.faos.controller;

import com.faos.exception.InvalidEntityException;
import com.faos.model.User;
import com.faos.enums.ConnectionStatus;
import com.faos.repositories.UserRepository;
import com.faos.enums.UserRole;
import com.faos.service.UserService;
import com.faos.service.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Collections;

@CrossOrigin(origins = "http://localhost:1000") // ✅ Allow frontend requests
@RestController
@RequestMapping("/api/users")

public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private UserRepository userRepository;

    // ✅ Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    // ✅ Get user by ID (admin access)
    @GetMapping("/{userId}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable Long userId) {
        try {
            Optional<User> user = userService.getUserByIdAdmin(userId);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Get a user by ID (Returns 404 if not found)
    @GetMapping("/{role}/{id}/{password}")
    public ResponseEntity<?> getUserById(@PathVariable String role, @PathVariable Long id, @PathVariable String password) {
        try {
            User user = userService.getUserById(id, password);
            
            // Convert string to UserRole enum
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            
            // Verify that the user has the specified role
            if (user.getRole() != null && user.getRole() == userRole) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. User does not have the required role: " + role);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User with ID " + id + " not found.");
        }
    }

    @GetMapping("/check-email")
    public Map<String, Boolean> checkEmailAvailability(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return Collections.singletonMap("available", !exists);
    }
    
    @GetMapping("/check-phone")
    public Map<String, Boolean> checkPhoneAvailability(@RequestParam String phone) {
        boolean exists = userRepository.existsByPhone(phone);
        return Collections.singletonMap("available", !exists);
    }

    // ✅ Add a new user
    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        User saved = userService.addUser(user);
        emailSenderService.sendRegistrationEmail(saved);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // ✅ Update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody User updatedUser) {
        User updated = userService.updateUser(id, updatedUser);
        if(updated != null) {
        	emailSenderService.sendUpdateNotificationEmail(updated);
        	return ResponseEntity.ok(updated);
        } else {
            throw new InvalidEntityException("User not found with ID: " + id);
        }
        	
    }

    // Get Active Users
    @GetMapping("/viewActiveUsers")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        
        return ResponseEntity.ok(users);
    }

    // Get Inactive Users
    @GetMapping("/viewInactiveUsers")
    public ResponseEntity<List<User>> getInactiveUsers() {
        List<User> users = userService.getInactiveUsers();
        return ResponseEntity.ok(users);
    }

 // Modify the existing deactivateUser endpoint to support both actions
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable @Min(1) Long userId) throws InvalidEntityException {
        Optional<User> optionalUser = userService.getUserByIdAdmin(userId);
        
        if (!optionalUser.isPresent()) {
            throw new InvalidEntityException("user not found with ID: " + userId);
        }
        
        User user = optionalUser.get();
        String response;
        
        // Check current status and toggle it
        if (user.getConnectionStatus() == ConnectionStatus.ACTIVE) {
            response = userService.setUserInactive(userId);
            emailSenderService.sendDeactivationEmail(user);
        } else {
            response = userService.setUserActive(userId);
            emailSenderService.sendActivationEmail(user);
        }
        
        return ResponseEntity.ok(response);
    }

    // Add a more general endpoint to toggle status that utilizes the existing endpoint
    @PutMapping("/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable @Min(1) Long userId,
            @RequestParam String status) throws InvalidEntityException {
        
        Optional<User> optionalUser = userService.getUserByIdAdmin(userId);
        
        if (!optionalUser.isPresent()) {
            throw new InvalidEntityException("User not found with ID: " + userId);
        }
        
        User user = optionalUser.get();
        
        // If the requested status matches current status, no change needed
        if ((status.equals("ACTIVE") && user.getConnectionStatus() == ConnectionStatus.ACTIVE) ||
            (status.equals("INACTIVE") && user.getConnectionStatus() == ConnectionStatus.INACTIVE)) {
            return ResponseEntity.ok("User is already in " + status + " status");
        }
        
        // Otherwise, use the existing deactivate endpoint to toggle the status
        return deactivateUser(userId);
    }

}
