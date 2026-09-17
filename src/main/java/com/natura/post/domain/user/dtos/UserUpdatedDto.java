package com.natura.post.domain.user.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para atualização do usuário (todos os campos são opcionais)")
public record UserUpdatedDto(
        @Schema(description = "Nome do vendedor", example = "João Silva Atualizado")
        String sellerName,

        @Schema(description = "Email do usuário", example = "joao.novo@email.com.br")
        String email,

        @Schema(description = "Número de telefone", example = "(11) 88888-8888")
        String phoneNumber) {
}
