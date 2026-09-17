package com.natura.post.domain.user;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.natura.post.domain.user.dtos.UserCreateDto;
import com.natura.post.domain.user.dtos.UserResponseDto;
import com.natura.post.domain.user.dtos.UserUpdatedDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado",
                    content = @Content)
    })
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto userDto) {
        User user = userService.createUser(userDto);
        UserResponseDto response = new UserResponseDto(
                user.getId(), user.getSellerName(), user.getEmail(), user.getPhoneNumber());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar usuário por email", description = "Retorna um usuário pelo seu endereço de email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<UserResponseDto> getUserByEmail(
            @Parameter(description = "Email do usuário", example = "usuario@email.com.br") @PathVariable String email) {
        User user = userService.findByEmail(email);
        UserResponseDto response = new UserResponseDto(
                user.getId(), user.getSellerName(), user.getEmail(), user.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna um usuário pelo seu ID único", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(description = "ID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id) {
        User user = userService.getUserById(id);
        UserResponseDto response = new UserResponseDto(
                user.getId(), user.getSellerName(), user.getEmail(), user.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<UserResponseDto> updateUserById(
            @Parameter(description = "ID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id,
            @Valid @RequestBody UserUpdatedDto userUpdatedDto) {
        User user = userService.updatedUserById(id, userUpdatedDto);
        UserResponseDto response = new UserResponseDto(user.getId(), user.getSellerName(), user.getEmail(),
                user.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário", description = "Remove um usuário do sistema", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteUserById(
            @Parameter(description = "ID do usuário", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

}
