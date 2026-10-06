package com.natura.post.domain.products.dtos.SocialImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class SocialImageRequestDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void semProductIdRetornaViolacao() {
        SocialImageRequestDto request = request(
                new SocialProductDto("Creme Hidratante", 89.90, null, null, null));

        Set<ConstraintViolation<SocialImageRequestDto>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        ConstraintViolation<SocialImageRequestDto> violation = violations.iterator().next();
        assertEquals("products[0].productId", violation.getPropertyPath().toString());
        assertEquals("O productId e obrigatorio", violation.getMessage());
    }

    @Test
    void comProductIdNaoRetornaViolacao() {
        SocialImageRequestDto request = request(
                new SocialProductDto("Creme Hidratante", 89.90, null, null, UUID.randomUUID()));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void listaVaziaRetornaViolacao() {
        SocialImageRequestDto request = new SocialImageRequestDto(List.of());

        Set<String> fields = validator.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("products"), fields);
    }

    private static SocialImageRequestDto request(SocialProductDto product) {
        return new SocialImageRequestDto(List.of(product));
    }
}
