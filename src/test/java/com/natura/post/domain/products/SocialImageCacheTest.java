package com.natura.post.domain.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.natura.post.domain.exception.ResourceNotFoundException;
import com.natura.post.domain.products.dtos.SocialImage.SocialImageResponseDto;
import com.natura.post.domain.products.dtos.SocialImage.SocialProductDto;
import com.natura.post.domain.user.User;

@ExtendWith(MockitoExtension.class)
class SocialImageCacheTest {

    @Mock
    private ProductStorageService storageService;

    @Mock
    private SocialImageCacheRepository socialImageCacheRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SocialImageService service;

    private UUID requester;
    private UUID productId;

    @BeforeEach
    void setUp() {
        requester = UUID.randomUUID();
        productId = UUID.randomUUID();
        lenient().when(productRepository.findAllById(any())).thenReturn(List.of(
                Products.builder()
                        .id(productId)
                        .user(User.builder().id(requester).build())
                        .build()));
    }

    private SocialProductDto product(double price) {

        return new SocialProductDto("Creme Hidratante", price, null, null, productId);
    }

    @Test
    void primeiraVezGeraERegistra() {
        when(socialImageCacheRepository.findByCacheKey(anyString()))
                .thenReturn(Optional.empty());

        SocialImageResponseDto response =
                service.generateSocialImages(List.of(product(89.90)), requester);

        verify(storageService, times(1)).uploadBytes(any(), anyString(), anyString());
        verify(socialImageCacheRepository, times(1)).save(any(SocialImageCache.class));
        assertEquals(Boolean.FALSE, response.images().get(0).reused());
    }

    @Test
    void segundaVezReaproveitaSemGerarNada() {
        SocialImageCache cached = SocialImageCache.builder()
                .cacheKey("abc")
                .r2Key("social/antigo.png")
                .imageUrl("https://cdn/social/antigo.png")
                .build();

        when(socialImageCacheRepository.findByCacheKey(anyString()))
                .thenReturn(Optional.of(cached));
        when(storageService.exists("social/antigo.png")).thenReturn(true);

        SocialImageResponseDto response =
                service.generateSocialImages(List.of(product(89.90)), requester);

        verify(storageService, never()).uploadBytes(any(), anyString(), anyString());
        verify(socialImageCacheRepository, never()).save(any(SocialImageCache.class));

        assertEquals("https://cdn/social/antigo.png", response.images().get(0).imageUrl());
        assertEquals(Boolean.TRUE, response.images().get(0).reused());
    }

    @Test
    void registroExisteMasArquivoSumiu_GeraDeNovoEAtualiza() {
        SocialImageCache cached = SocialImageCache.builder()
                .id(UUID.randomUUID())
                .cacheKey("abc")
                .r2Key("social/apagado.png")
                .imageUrl("https://cdn/social/apagado.png")
                .build();

        when(socialImageCacheRepository.findByCacheKey(anyString()))
                .thenReturn(Optional.of(cached));
        when(storageService.exists("social/apagado.png")).thenReturn(false);
        when(storageService.uploadBytes(any(), anyString(), anyString()))
                .thenReturn("https://cdn/social/novo.png");

        SocialImageResponseDto response =
                service.generateSocialImages(List.of(product(89.90)), requester);

        verify(storageService, times(1)).uploadBytes(any(), anyString(), anyString());

        ArgumentCaptor<SocialImageCache> captor =
                ArgumentCaptor.forClass(SocialImageCache.class);
        verify(socialImageCacheRepository, times(1)).save(captor.capture());

        assertEquals(cached.getId(), captor.getValue().getId());
        assertEquals("https://cdn/social/novo.png", captor.getValue().getImageUrl());
        assertEquals(Boolean.FALSE, response.images().get(0).reused());
    }

    @Test
    void precoDiferenteGeraImagemSeparada() {
        when(socialImageCacheRepository.findByCacheKey(anyString()))
                .thenReturn(Optional.empty());

        service.generateSocialImages(List.of(product(89.90), product(99.90)), requester);

        verify(storageService, times(2)).uploadBytes(any(), anyString(), anyString());
        verify(socialImageCacheRepository, times(2)).save(any(SocialImageCache.class));
    }

    @Test
    void productIdDeOutroUsuarioRetorna404() {
        UUID produtoDeOutrem = UUID.randomUUID();

        Products alheio = Products.builder()
                .id(produtoDeOutrem)
                .user(User.builder().id(UUID.randomUUID()).build())
                .build();
        when(productRepository.findAllById(any())).thenReturn(List.of(alheio));

        SocialProductDto roubado = new SocialProductDto(
                "Creme", 89.90, null, null, produtoDeOutrem);

        assertThrows(ResourceNotFoundException.class,
                () -> service.generateSocialImages(List.of(roubado), requester));

        verify(storageService, never()).uploadBytes(any(), anyString(), anyString());
    }

    @Test
    void comProductIdValidoUsaOCache() {
        when(socialImageCacheRepository.findByCacheKey(anyString()))
                .thenReturn(Optional.empty());

        service.generateSocialImages(List.of(product(89.90)), requester);
        service.generateSocialImages(List.of(product(89.90)), requester);

        verify(storageService, times(2)).uploadBytes(any(), anyString(), anyString());
    }

    @Test
    void semProductIdLancaExcecao() {
        SocialProductDto semId = new SocialProductDto(
                "Creme Hidratante", 89.90, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.generateSocialImages(List.of(semId), requester));

        verify(storageService, never()).uploadBytes(any(), anyString(), anyString());
    }
}
