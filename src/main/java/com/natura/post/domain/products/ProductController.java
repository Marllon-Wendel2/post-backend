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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.natura.post.domain.products.dtos.CreateProductDto;
import com.natura.post.domain.products.dtos.ProductResponseDto;
import com.natura.post.domain.products.dtos.ProductSearchDto;
import com.natura.post.domain.products.dtos.SocialImage.SocialImageRequestDto;
import com.natura.post.domain.products.dtos.SocialImage.SocialImageResponseDto;
import com.natura.post.domain.products.dtos.UpdateProductDto;
import com.natura.post.domain.user.User;

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
import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos e imagens sociais")
public class ProductController {

        private final ProductService productService;
        private final SocialImageService socialImageService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Criar produto", description = "Cria um novo produto com imagem. O produto deve ser enviado como JSON no campo 'product' e a imagem no campo 'image'", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso", content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
        })
        public ResponseEntity<ProductResponseDto> createProduct(
                        @Parameter(description = "Dados do produto em formato JSON", required = true, schema = @Schema(type = "string", format = "binary")) @RequestParam("product") String productJsonString,
                        @Parameter(description = "Imagem do produto (JPEG, PNG)", required = true, schema = @Schema(type = "string", format = "binary")) @RequestPart("image") MultipartFile image) {

                ObjectMapper objectMapper = new ObjectMapper();
                CreateProductDto productDto = objectMapper.readValue(productJsonString, CreateProductDto.class);
                User user = getCurrentUser();

                ProductResponseDto response = productService.createProducts(productDto, image, user);

                return ResponseEntity.status(201).body(response);
        }

        @GetMapping("/search")
        @Operation(
                summary = "Buscar produtos",
                description = "Busca produtos do usuário autenticado com filtros opcionais (nome, preço mínimo e máximo)",
                security = @SecurityRequirement(name = "Bearer Authentication")
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso",
                        content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                @ApiResponse(responseCode = "401", description = "Não autenticado",
                        content = @Content)
        })
        public ResponseEntity<List<ProductResponseDto>> searchProducts(ProductSearchDto searchDto) {
                User user = getCurrentUser();
                return ResponseEntity.ok(productService.searchProducts(searchDto, user.getId()));
        }

        @GetMapping("/user/{userId}")
        @Operation(summary = "Listar produtos por usuário", description = "Retorna todos os produtos de um específico usuário", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso", content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
        })
        public ResponseEntity<List<ProductResponseDto>> getProductByUserId(
                        @Parameter(description = "ID do usuário", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID userId) {
                List<ProductResponseDto> products = productService.getProductByUserId(userId);
                return ResponseEntity.ok(products);
        }

        @GetMapping("/{productId}")
        @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico pelo seu ID", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Produto encontrado", content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
        })
        public ResponseEntity<ProductResponseDto> getProductById(
                        @Parameter(description = "ID do produto", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID productId) {
                ProductResponseDto product = productService.getProductById(productId);
                return ResponseEntity.ok(product);
        }

        @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Atualizar produto", description = "Atualiza um produto existente. Pode atualizar dados e/ou imagem", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso", content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
        })
        public ResponseEntity<ProductResponseDto> updateProductById(
                        @Parameter(description = "ID do produto", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id,
                        @Parameter(description = "Dados atualizados do produto em formato JSON", required = false, schema = @Schema(type = "string", format = "binary")) @RequestPart(value = "product", required = false) UpdateProductDto productDto,
                        @Parameter(description = "Nova imagem do produto", required = false, schema = @Schema(type = "string", format = "binary")) @RequestPart(value = "image", required = false) MultipartFile image) {
                User user = getCurrentUser();
                ProductResponseDto response = productService.updateProductById(id, productDto, image, user);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{productId}")
        @Operation(summary = "Deletar produto", description = "Remove um produto do sistema", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
                        @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
        })
        public ResponseEntity<Void> deleteProductById(
                        @Parameter(description = "ID do produto", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID productId) {
                User user = getCurrentUser();
                productService.deleteProductById(productId, user);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/social-image")
        @Operation(summary = "Gerar imagens para redes sociais", description = "Gera imagens otimizadas para postagem em redes sociais a partir de uma lista de produtos", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Imagens geradas com sucesso", content = @Content(schema = @Schema(implementation = SocialImageResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content)
        })
        public ResponseEntity<SocialImageResponseDto> generateSocialImages(
                        @Valid @RequestBody SocialImageRequestDto request) {
                SocialImageResponseDto response = socialImageService.generateSocialImages(request.products());
                return ResponseEntity.ok(response);
        }

        private User getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                return (User) authentication.getPrincipal();
        }
}
