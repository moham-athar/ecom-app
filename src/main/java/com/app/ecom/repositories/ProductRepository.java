package com.app.ecom.repositories;

import com.app.ecom.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndActiveTrue(Long id);

    @Query("""
    SELECT p
    FROM Product p
    WHERE p.active = true
      AND p.quantity > 0
      AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
""")
    List<Product> searchProduct(@Param("name") String name);
}
