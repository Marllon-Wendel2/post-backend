package com.natura.post.domain.products.dtos.SocialImage;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de um produto para geração de imagem social")
public record SocialProductDto(
        @Schema(description = "Título do produto", example = "Creme Hidratante Natura")
        String title,

        @Schema(description = "Preço do produto", example = "89.90")
        Double price,

        @Schema(description = "URL da imagem do produto", example = "https://storage.naturapost.com.br/products/image.jpg")
        String imageUrl) {
}
