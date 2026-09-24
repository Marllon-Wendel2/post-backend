package com.natura.post.domain.products;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.natura.post.domain.exception.ResourceNotFoundException;
import com.natura.post.domain.products.dtos.CreateProductDto;
import com.natura.post.domain.products.dtos.ProductResponseDto;
import com.natura.post.domain.products.dtos.ProductSearchDto;
import com.natura.post.domain.products.dtos.UpdateProductDto;
import com.natura.post.domain.user.User;

import static com.natura.post.utils.UpdateUtils.updateIfPresent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductStorageService storageService;
    private final ProductStorageService productStorageService;

    public ProductResponseDto createProducts(CreateProductDto createProductDto, MultipartFile image, User user) {

        String imageUrl = productStorageService.uploadFile(image);

        Products product = Products.builder()
                .title(createProductDto.title())
                .price(createProductDto.price())
                .imageUrl(imageUrl)
                .isActive(true)
                .user(user)
                .build();

        Products savedProduct = productRepository.save(product);
        return toResponseDto(savedProduct);
    }

    public List<ProductResponseDto> getProductByUserId(UUID userId) {
        List<Products> products = productRepository.findByUserId(userId);
        return products.stream().map(this::toResponseDto).toList();
    }

    public ProductResponseDto getProductById(UUID productId) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado!"));

        return toResponseDto(product);
    }

    public ProductResponseDto updateProductById(
            UUID id,
            UpdateProductDto updateProductDto,
            MultipartFile image,
            User user) {
        Products product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + id));

        if (!product.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Você não tem permissão para editar este produto");
        }

        if (updateProductDto != null) {
            updateIfPresent(updateProductDto.title(), product::setTitle);
            updateIfPresent(updateProductDto.price(), product::setPrice);
            updateIfPresent(updateProductDto.isActive(), product::setIsActive);
        }

        String oldImageUrl = product.getImageUrl();

        if (image != null && !image.isEmpty()) {
            String newImageUrl = storageService.uploadFile(image);
            product.setImageUrl(newImageUrl);

            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                storageService.deleteFile(oldImageUrl);
            }

        }

        Products updatedProduct = productRepository.save(product);
        return toResponseDto(updatedProduct);
    }

    public void deleteProductById(UUID productId, User user) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto nao encontrado com o ID: " + productId));

        if (!product.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Voce nao tem permissao para deletar este produto");
        }

        storageService.deleteFile(product.getImageUrl());

        productRepository.delete(product);
    }

    public List<ProductResponseDto> searchProducts(ProductSearchDto productSearchDto, UUID userId) {
        return productRepository
                .search(userId, productSearchDto.name(), productSearchDto.minPrice(), productSearchDto.maxPrice())
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    private ProductResponseDto toResponseDto(Products product) {
        return new ProductResponseDto(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getImageUrl(),
                product.getIsActive(),
                product.getUser().getId());
    }
}
