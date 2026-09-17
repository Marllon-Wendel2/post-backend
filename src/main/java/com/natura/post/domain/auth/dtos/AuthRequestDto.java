package com.natura.post.domain.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados de entrada para o login.
 * 
 * Usado quando o usuário envia email e senha para autenticar.
 */
@Schema(description = "Dados de entrada para autenticação")
public record AuthRequestDto(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Utilize um e-mail válido")
        @Schema(description = "Email do usuário", example = "usuario@email.com.br", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Schema(description = "Senha do usuário", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password) {
}
