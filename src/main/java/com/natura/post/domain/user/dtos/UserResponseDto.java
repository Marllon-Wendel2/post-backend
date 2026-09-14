package com.natura.post.domain.user.dtos;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String sellerName,
        String email,
        String phoneNumber) {
}
