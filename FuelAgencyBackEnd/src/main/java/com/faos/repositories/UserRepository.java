package com.faos.repositories;

import com.faos.model.User;
import com.faos.enums.ConnectionStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	List<User> findByConnectionStatus(ConnectionStatus connectionStatus);
    Optional<User> findByEmail(String email);
    Optional<User> findByuserIdAndPassword(Long userId, String password);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
