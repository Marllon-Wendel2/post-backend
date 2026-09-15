package com.natura.post.domain.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados de entrada para o login.
 * 
 * Usado quando o usuário envia email e senha para autenticar.
 */
public record AuthRequestDto(
        @NotBlank(message = "Email é obrigatório") @Email(message = "Utilize um e-mail válido") String email,
        @NotBlank(message = "Senha é obrigatória") String password) {
}
