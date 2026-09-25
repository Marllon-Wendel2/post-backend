package com.natura.post.domain.auth.dtos;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dados de saída após login bem-sucedido.
 * 
 * Contém os tokens que o frontend deve salvar e usar
 * para acessar rotas protegidas.
 */
@Schema(description = "Resposta após autenticação bem-sucedida")
public record AuthResponseDto(
                @Schema(description = "Token de acesso (válido por 24 horas)", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U") String token,

                @Schema(description = "Token de renovação (válido por 7 dias)", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.refresh_token") String refreshToken,

                @Schema(description = "Email do usuário autenticado", example = "usuario@email.com.br") String email,

                @Schema(description = "ID do usuário") UUID id) {
}
