package com.natura.post.domain.products.dtos.SocialImage;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com as imagens sociais geradas")
public record SocialImageResponseDto(
        @Schema(description = "Lista de imagens geradas para redes sociais")
        List<SocialImageItemDto> images) {
}
