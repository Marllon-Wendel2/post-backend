package com.natura.post.domain.products.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductDto(
        @NotBlank(message = "Informe o titulo do produto") String title,

        @NotNull(message = "O preco do produto e obrigatorio") @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero") Double price) {
}
