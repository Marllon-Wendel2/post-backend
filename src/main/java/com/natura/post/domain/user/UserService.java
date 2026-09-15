package com.natura.post.domain.user;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.natura.post.domain.user.dtos.UserCreateDto;
import com.natura.post.domain.user.dtos.UserUpdatedDto;
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

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com o email: " + email));
    }

    public User updatedUserById(UUID id, UserUpdatedDto userUpdatedDto) {
        User user = getUserById(id);

        if (userUpdatedDto.sellerName() != null) {
            user.setSellerName(userUpdatedDto.sellerName());
        }

        if (userUpdatedDto.phoneNumber() != null) {
            user.setPhoneNumber(userUpdatedDto.phoneNumber());
        }

        if (userUpdatedDto.email() != null) {
            user.setEmail(userUpdatedDto.email());
        }

        return userRepository.save(user);
    }

    public void deleteUserById(UUID id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

}
