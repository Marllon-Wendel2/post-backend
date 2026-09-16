package com.natura.post.domain.products.dtos;

import java.util.UUID;

public record ProductResponseDto(
        UUID id,
        String title,
        Double price,
        String imageUrl,
        Boolean isActive,
        UUID userId) {
}
