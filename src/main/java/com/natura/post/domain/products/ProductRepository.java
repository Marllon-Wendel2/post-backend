package com.natura.post.domain.products;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Products, UUID> {
    List<Products> findByUserId(UUID userId);

    @Query("SELECT p FROM Products p WHERE " +
            "p.user.id = :userId AND " +
            "(:name IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Products> search(@Param("userId") UUID userId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);
}
