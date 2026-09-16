package com.natura.post.domain.products;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.natura.post.domain.products.dtos.SocialImage.SocialImageItemDto;
import com.natura.post.domain.products.dtos.SocialImage.SocialImageResponseDto;
import com.natura.post.domain.products.dtos.SocialImage.SocialProductDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialImageService {

    private static final Logger logger = LoggerFactory.getLogger(SocialImageService.class);

    private final ProductStorageService storageService;

    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1080;

    // Paleta Clássica / Editorial
    private static final Color BACKGROUND_COLOR = new Color(253, 251, 247); // Creme / Papel Antigo
    private static final Color BORDER_COLOR = new Color(212, 197, 169); // Tom dourado/bronze sutil
    private static final Color TEXT_PRIMARY = new Color(44, 42, 41); // Marrom escuro clássico (quase preto)
    private static final Color TEXT_ACCENT = new Color(139, 69, 19); // Tom Madeira / Cobre clássico

    public SocialImageResponseDto generateSocialImages(List<SocialProductDto> products) {
        List<SocialImageItemDto> results = new ArrayList<>();

        for (SocialProductDto product : products) {
            try {
                byte[] imageBytes = generateSingleImage(product);
                String fileName = "social/" + UUID.randomUUID() + ".png";
                String r2Url = storageService.uploadBytes(imageBytes, fileName, "image/png");

                results.add(new SocialImageItemDto(product.title(), r2Url, product.price()));
                logger.info("Imagem clássica gerada com sucesso para: {} -> {}", product.title(), r2Url);
            } catch (Exception e) {
                logger.error("Erro ao gerar imagem para o produto '{}': {}", product.title(), e.getMessage());
            }
        }

        return new SocialImageResponseDto(results);
    }

    private byte[] generateSingleImage(SocialProductDto product) throws IOException {
        BufferedImage canvas = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();

        // Alta qualidade de renderização para curvas suaves nas letras
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 1. Fundo tom Creme Clássico
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 2. Moldura Externa Dupla (Estilo Editorial / Clássico)
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(50, 50, IMAGE_WIDTH - 100, IMAGE_HEIGHT - 100);
        g2d.drawRect(60, 60, IMAGE_WIDTH - 120, IMAGE_HEIGHT - 120);

        // 3. Renderiza a Imagem do Produto centralizada
        if (product.imageUrl() != null && !product.imageUrl().isEmpty()) {
            try {
                BufferedImage productImage = ImageIO.read(new URL(product.imageUrl()));
                if (productImage != null) {
                    int imgSize = 580;
                    int imgX = (IMAGE_WIDTH - imgSize) / 2;
                    int imgY = 140;

                    // Fundo branco interno para destacar a foto do produto
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(imgX - 20, imgY - 20, imgSize + 40, imgSize + 40);
                    g2d.setColor(new Color(230, 220, 205));
                    g2d.drawRect(imgX - 20, imgY - 20, imgSize + 40, imgSize + 40);

                    g2d.drawImage(productImage, imgX, imgY, imgSize, imgSize, null);
                }
            } catch (Exception e) {
                logger.warn("Nao foi possivel carregar a imagem do produto: {}", product.imageUrl());
            }
        }

        // 4. Tipografia Clássica (Usando Serif / Georgia para o toque tradicional)
        // Nome da Marca ou Categoria sutil no topo inferior
        g2d.setColor(TEXT_ACCENT);
        g2d.setFont(new Font("Georgia", Font.ITALIC, 24));
        g2d.drawString("— Promoção Exclusiva —",
                (IMAGE_WIDTH - g2d.getFontMetrics().stringWidth("— Coleção Exclusiva —")) / 2, 810);

        // 5. Título do Produto (Estilo Clássico / Serifado)
        g2d.setColor(TEXT_PRIMARY);
        g2d.setFont(new Font("Georgia", Font.BOLD, 40));

        String title = product.title() != null ? product.title() : "Essência Natura";
        FontMetrics titleMetrics = g2d.getFontMetrics();
        int maxWidth = IMAGE_WIDTH - 200;
        String truncatedTitle = truncateText(title, titleMetrics, maxWidth);

        int titleX = (IMAGE_WIDTH - titleMetrics.stringWidth(truncatedTitle)) / 2;
        g2d.drawString(truncatedTitle, titleX, 880);

        // 6. Preço em destaque clássico
        g2d.setColor(TEXT_ACCENT);
        g2d.setFont(new Font("TimesNewRoman", Font.BOLD, 48));
        String priceText = product.price() != null ? String.format("R$ %.2f", product.price()) : "";
        int priceX = (IMAGE_WIDTH - g2d.getFontMetrics().stringWidth(priceText)) / 2;
        g2d.drawString(priceText, priceX, 955);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(canvas, "png", baos);
        return baos.toByteArray();
    }

    private String truncateText(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        while (fm.stringWidth(text + "...") > maxWidth && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }
}