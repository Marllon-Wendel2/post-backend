package com.natura.post.domain.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.natura.post.domain.auth.dtos.AuthRequestDto;
import com.natura.post.domain.auth.dtos.AuthResponseDto;
import com.natura.post.domain.auth.dtos.RefreshTokenRequestDto;
import com.natura.post.domain.exception.ResourceNotFoundException;
import com.natura.post.domain.user.User;
import com.natura.post.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthResponseDto login(AuthRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com o email: " + request.email()));

        String token = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshTokenString(user.getEmail());

        return new AuthResponseDto(token, refreshToken, user.getEmail());
    }

    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {

        if (!jwtService.isRefreshTokenValid(request.refreshToken())) {
            throw new RuntimeException("Refresh token inválido");
        }

        String email = jwtService.extractEmail(request.refreshToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com o email: " + email));

        String newToken = jwtService.generateToken(user.getEmail());
        String newRefreshToken = jwtService.generateRefreshTokenString(user.getEmail());

        return new AuthResponseDto(newToken, newRefreshToken, user.getEmail());
    }
}
