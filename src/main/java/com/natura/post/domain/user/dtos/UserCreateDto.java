package com.natura.post.domain.user.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Dados para criação de um novo usuário")
public record UserCreateDto(
        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome do vendedor", example = "João Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        String sellerName,

        @NotBlank(message = "Email é obrigatório para registro")
        @Email(message = "Utilize um e-mail válido, por exemplo: nome@dominio.com.br")
        @Schema(description = "Email do usuário (será usado para login)", example = "joao@email.com.br", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "Informe um número de contato")
        @Schema(description = "Número de telefone para contato", example = "(11) 99999-9999", requiredMode = Schema.RequiredMode.REQUIRED)
        String phoneNumber,

        @NotBlank(message = "Senha é obrigatória")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!_.-]).{8,}$", message = "A senha deve ter no mínimo 8 caracteres, pelo menos uma letra maiúscula, uma minúscula e um caractere especial.")
        @Schema(description = "Senha do usuário (mínimo 8 caracteres, 1 maiúscula, 1 minúscula e 1 especial)", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password) {
}
