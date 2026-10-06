package com.natura.post.domain.products;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialImageCacheRepository extends JpaRepository<SocialImageCache, UUID> {

    Optional<SocialImageCache> findByCacheKey(String cacheKey);
}
