package com.natura.post.domain.products.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Filtros para busca de produtos")
public record ProductSearchDto(
        @Schema(description = "Nome/título do produto (busca parcial, case-insensitive)", example = "creme")
        String name,

        @Schema(description = "Preço mínimo", example = "50.00")
        Double minPrice,

        @Schema(description = "Preço máximo", example = "200.00")
        Double maxPrice) {
}
