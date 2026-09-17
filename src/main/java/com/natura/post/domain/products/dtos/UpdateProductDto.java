package com.natura.post.domain.products.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para atualização do produto (todos os campos são opcionais)")
public record UpdateProductDto(
        @Schema(description = "Título do produto", example = "Creme Hidratante Natura - Nova Fórmula")
        String title,

        @Schema(description = "Preço do produto", example = "99.90")
        Double price,

        @Schema(description = "Status de ativação do produto", example = "true")
        Boolean isActive) {
}
