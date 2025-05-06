package com.faos.repositories;

import com.faos.model.EntityStatus;
import com.faos.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findAllByStatus(EntityStatus status);
    Optional<Supplier> findBySupplierIdAndStatus(Long supplierId, EntityStatus status);
    List<Supplier> findByStatus(EntityStatus status);
    Optional<Supplier> findById(Long supplierId);
    // Custom query using JPA naming convention
    List<Supplier> findByNameContainingIgnoreCase(String name);

    // Custom query using @Query annotation
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Supplier> searchByName(@Param("name") String name);

    // Custom query for exact match (case insensitive)
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) = LOWER(:name)")
    List<Supplier> findByNameExactIgnoreCase(@Param("name") String name);
}