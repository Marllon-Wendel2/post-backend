package com.natura.post.domain.products.dtos.SocialImage;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados de entrada para geração de imagens para redes sociais")
public record SocialImageRequestDto(
        @NotNull(message = "A lista de produtos e obrigatoria")
        @NotEmpty(message = "Envie pelo menos um produto para gerar a imagem")
        @Schema(description = "Lista de produtos para gerar as imagens", requiredMode = Schema.RequiredMode.REQUIRED)
        List<SocialProductDto> products) {
}
