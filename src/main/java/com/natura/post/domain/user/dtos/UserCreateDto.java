package com.natura.post.domain.user.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserCreateDto(
        @NotBlank(message = "Nome é obrigatório") String sellerName,

        @NotBlank(message = "Email é obrigatório para registro") @Email(message = "Utilize um e-mail válido, por exemplo: nome@dominio.com.br") String email,

        @NotBlank(message = "Informe um número de contato") String phoneNumber,

        @NotBlank(message = "Senha é obrigatória") @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!_.-]).{8,}$", message = "A senha deve ter no mínimo 8 caracteres, pelo menos uma letra maiúscula, uma minúscula e um caractere especial.") String password) {
}
