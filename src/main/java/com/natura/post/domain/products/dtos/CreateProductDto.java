package com.natura.post.domain.products.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para criação de um novo produto")
public record CreateProductDto(
        @NotBlank(message = "Informe o titulo do produto")
        @Schema(description = "Título do produto", example = "Creme Hidratante Natura", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotNull(message = "O preco do produto e obrigatorio")
        @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero")
        @Schema(description = "Preço do produto", example = "89.90", requiredMode = Schema.RequiredMode.REQUIRED)
        Double price) {
}
