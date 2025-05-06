package com.faos.service;
import com.faos.exception.InvalidEntityException;
import com.faos.exception.ResourceNotFoundException;
import com.faos.exception.DuplicateEntityException;
import com.faos.model.User;
import com.faos.enums.ConnectionStatus;
import com.faos.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailSenderService emailService;

    private final Random random = new Random();

    // ✅ Generate a unique 10-digit user ID
    private Long generateUniqueUserId() {
        Long newId;
        do {
            newId = 1000000000L + random.nextInt(900000000); // Generates a 10-digit number
        } while (userRepository.existsById(newId)); // Ensures uniqueness
        return newId;
    }

    // ✅ Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ Get user by ID and password
    public User getUserById(Long userId, String password) {
        return userRepository.findByuserIdAndPassword(userId, password)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
    
    // ✅ Get user by ID through Admin
    public Optional<User> getUserByIdAdmin(Long id) {
        return userRepository.findById(id);
    }

    // ✅ Add a new user with a unique 10-digit ID
    @Transactional
    public User addUser(User user) {
        
    // Check if email already exists
    if (userRepository.existsByEmail(user.getEmail())) {
        throw new DuplicateEntityException("User with email " + user.getEmail() + " already exists");
    }
    
    // Check if phone already exists (if phone is provided)
    if (user.getPhone() != null && !user.getPhone().isEmpty() && 
        userRepository.existsByPhone(user.getPhone())) {
        throw new DuplicateEntityException("User with phone number " + user.getPhone() + " already exists");
    }



    	String Password = UUID.randomUUID().toString().substring(0, 8);
    	
        if (user.getUserId() == null || user.getPassword() == null) {
            user.setUserId(generateUniqueUserId());
            user.setPassword(Password);
        }
        
        User savedUser = userRepository.save(user);
        
     // Send login credentials via email
        emailService.sendRegistrationEmail(savedUser.getUserId(),savedUser.getEmail(), Password);
        
        return savedUser;
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhone(updatedUser.getPhone());
            existingUser.setAddress(updatedUser.getAddress());
            existingUser.setConnectionType(updatedUser.getConnectionType());
            return userRepository.save(existingUser); // Save updated user
        } else {
            return null; // User not found
        }
    }

    public List<User> getActiveUsers() {
    	
        return userRepository.findByConnectionStatus(ConnectionStatus.ACTIVE);
    }

    public List<User> getInactiveUsers() {
        return userRepository.findByConnectionStatus(ConnectionStatus.INACTIVE);
    }

 // Add this method to your UserService class
    @Transactional
    public String setUserActive(Long userId) throws InvalidEntityException {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.getConnectionStatus() == ConnectionStatus.ACTIVE) {
                throw new InvalidEntityException("User is already active");
            }

            user.setConnectionStatus(ConnectionStatus.ACTIVE);
            userRepository.save(user);
            return "User status set to active successfully.";
        } else {
            throw new InvalidEntityException("User not found with ID: " + userId);
        }
    }
    
    //deactivating user
    @Transactional
    public String setUserInactive(Long userId) throws InvalidEntityException {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.getConnectionStatus() == ConnectionStatus.INACTIVE) {
                throw new InvalidEntityException("User is already inactive");
            }

            user.setConnectionStatus(ConnectionStatus.INACTIVE);
            userRepository.save(user);
            return "User status set to inactive successfully.";
        } else {
            throw new InvalidEntityException("User not found with ID: " + userId);
        }
    }


}
