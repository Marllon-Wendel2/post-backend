package com.natura.post.domain.user;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.natura.post.domain.user.dtos.UserCreateDto;
import com.natura.post.domain.user.dtos.UserResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto userDto) {
        User user = userService.createUser(userDto);
        UserResponseDto response = new UserResponseDto(
                user.getId(), user.getSellerName(), user.getEmail(), user.getPhoneNumber());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        UserResponseDto response = new UserResponseDto(
                user.getId(), user.getSellerName(), user.getEmail(), user.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

}
