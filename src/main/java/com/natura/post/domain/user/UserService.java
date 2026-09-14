package com.natura.post.domain.user;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.natura.post.domain.user.dtos.UserCreateDto;

import com.natura.post.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public User createUser(UserCreateDto userDto) {
        String hashPassword = passwordEncoder.encode(userDto.password());

        User user = User.builder()
                .sellerName(userDto.sellerName())
                .email(userDto.email())
                .phoneNumber(userDto.phoneNumber())
                .hashPassword(hashPassword)
                .build();

        return userRepository.save(user);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));
    }

}
