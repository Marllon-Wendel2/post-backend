package com.natura.post.domain.auth.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Dados de entrada para renovar o access token.
 * 
 * Quando o access token expira, o frontend envia o refresh token
 * para obter um novo access token sem precisar fazer login novamente.
 */
public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token é obrigatório") String refreshToken) {
}
