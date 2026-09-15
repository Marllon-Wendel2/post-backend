package com.natura.post.domain.auth.dtos;

/**
 * Dados de saída após login bem-sucedido.
 * 
 * Contém os tokens que o frontend deve salvar e usar
 * para acessar rotas protegidas.
 */
public record AuthResponseDto(
        String token, // Access token (vive 24 horas)
        String refreshToken, // Refresh token (vive 7 dias)
        String email // Email do usuário (para exibição)
) {
}