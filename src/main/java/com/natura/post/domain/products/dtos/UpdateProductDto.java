package com.natura.post.domain.products.dtos;

public record UpdateProductDto(
        String title,
        Double price,
        Boolean isActive) {
}
