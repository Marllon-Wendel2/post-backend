package com.natura.post.domain.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados de entrada para renovar o access token.
 * 
 * Quando o access token expira, o frontend envia o refresh token
 * para obter um novo access token sem precisar fazer login novamente.
 */
@Schema(description = "Dados para renovação do token de acesso")
public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token é obrigatório")
        @Schema(description = "Token de renovação obtido no login", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.refresh_token", requiredMode = Schema.RequiredMode.REQUIRED)
        String refreshToken) {
}
