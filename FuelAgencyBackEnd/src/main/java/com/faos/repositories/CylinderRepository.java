package com.faos.repositories;

import com.faos.model.Cylinder;
import com.faos.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CylinderRepository extends JpaRepository<Cylinder, Integer> {
    Optional<Cylinder> findByCylinderId(int cylinderId);
    int countBySupplierAndCylinderStatus(Supplier supplier, String cylinderStatus);
    List<Cylinder> findByCylinderStatus(String cylinderStatus);
    List<Cylinder> findByType(String type);
    List<Cylinder> findByCylinderStatusAndType(String cylinderStatus, String type);
}
