package com.natura.post.domain.products;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "social_images")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialImageCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cache_key", nullable = false, unique = true, length = 64)
    private String cacheKey;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_title")
    private String productTitle;

    @Column(name = "product_price")
    private Double productPrice;

    @Column(name = "product_image_url")
    private String productImageUrl;

    @Column(name = "brand")
    private String brand;

    @Column(name = "r2_key", nullable = false, length = 512)
    private String r2Key;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
