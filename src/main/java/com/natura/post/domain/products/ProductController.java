package com.natura.post.domain.products;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.natura.post.domain.products.dtos.CreateProductDto;
import com.natura.post.domain.products.dtos.ProductResponseDto;
import com.natura.post.domain.products.dtos.UpdateProductDto;
import com.natura.post.domain.user.User;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> createProduct(
            @RequestParam("product") String productJsonString,
            @RequestPart("image") MultipartFile image) {

        ObjectMapper objectMapper = new ObjectMapper();
        CreateProductDto productDto = objectMapper.readValue(productJsonString, CreateProductDto.class);
        User user = getCurrentUser();

        ProductResponseDto response = productService.createProducts(productDto, image, user);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProductResponseDto>> getProductByUserId(@PathVariable UUID userId) {
        List<ProductResponseDto> products = productService.getProductByUserId(userId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable UUID productId) {
        ProductResponseDto product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> updateProductById(
            @PathVariable UUID id,
            @RequestPart(value = "product", required = false) UpdateProductDto productDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        User user = getCurrentUser();
        ProductResponseDto response = productService.updateProductById(id, productDto, image, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductById(@PathVariable UUID productId) {
        User user = getCurrentUser();
        productService.deleteProductById(productId, user);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
