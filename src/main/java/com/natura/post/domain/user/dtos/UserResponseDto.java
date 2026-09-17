package com.natura.post.domain.user.dtos;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados do usuário retornado na resposta")
public record UserResponseDto(
        @Schema(description = "ID único do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Nome do vendedor", example = "João Silva")
        String sellerName,

        @Schema(description = "Email do usuário", example = "joao@email.com.br")
        String email,

        @Schema(description = "Número de telefone", example = "(11) 99999-9999")
        String phoneNumber) {
}
