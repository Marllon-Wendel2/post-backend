package com.natura.post.domain.products;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Products, UUID> {
    List<Products> findByUserId(UUID userId);

}
