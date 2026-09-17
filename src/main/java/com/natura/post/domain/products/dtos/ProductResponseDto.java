package com.natura.post.domain.products.dtos;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados do produto retornado na resposta")
public record ProductResponseDto(
        @Schema(description = "ID único do produto", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Título do produto", example = "Creme Hidratante Natura")
        String title,

        @Schema(description = "Preço do produto", example = "89.90")
        Double price,

        @Schema(description = "URL da imagem do produto", example = "https://storage.naturapost.com.br/products/image.jpg")
        String imageUrl,

        @Schema(description = "Indica se o produto está ativo", example = "true")
        Boolean isActive,

        @Schema(description = "ID do usuário proprietário do produto", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId) {
}
