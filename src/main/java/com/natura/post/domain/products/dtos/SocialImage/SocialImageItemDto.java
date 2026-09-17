package com.natura.post.domain.products.dtos.SocialImage;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de uma imagem social gerada")
public record SocialImageItemDto(
        @Schema(description = "Título original do produto", example = "Creme Hidratante Natura")
        String originalTitle,

        @Schema(description = "URL da imagem gerada", example = "https://storage.naturapost.com.br/social/image.jpg")
        String imageUrl,

        @Schema(description = "Preço utilizado na imagem", example = "89.90")
        Double priceUsed) {
}
