package com.natura.post.domain.products.dtos.SocialImage;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SocialImageRequestDto(
        @NotNull(message = "A lista de produtos e obrigatoria") @NotEmpty(message = "Envie pelo menos um produto para gerar a imagem") List<SocialProductDto> products) {
}
