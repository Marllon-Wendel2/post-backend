package com.natura.post.domain.products;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.natura.post.domain.exception.ResourceNotFoundException;
import com.natura.post.domain.products.dtos.SocialImage.SocialImageItemDto;
import com.natura.post.domain.products.dtos.SocialImage.SocialImageResponseDto;
import com.natura.post.domain.products.dtos.SocialImage.SocialProductDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialImageService {

    private static final Logger logger = LoggerFactory.getLogger(SocialImageService.class);

    private final ProductStorageService storageService;
    private final ProductRepository productRepository;
    private final SocialImageCacheRepository socialImageCacheRepository;

    private static final int CANVAS = 1080;

    private static final Color PAGE_BG = color("#EEEEEC");
    private static final Color CARD_BG = color("#292929");
    private static final Color SURFACE_LIGHT = color("#F2F2F2");
    private static final Color TEXT_LIGHT = color("#F5F5F5");
    private static final Color TEXT_MUTED = color("#B6B6B6");
    private static final Color WHITE = Color.WHITE;

    private static final String DEFAULT_BRAND = "Natura";

    private static final int CARD_X = 140;
    private static final int CARD_Y = 57;
    private static final int CARD_W = 800;
    private static final int CARD_H = 965;
    private static final int CARD_R = 64;
    private static final int CARD_PADDING = 16;

    private static final int IMAGE_X = CARD_X + CARD_PADDING;
    private static final int IMAGE_Y = CARD_Y + CARD_PADDING;
    private static final int IMAGE_W = CARD_W - 2 * CARD_PADDING;
    private static final int IMAGE_H = 600;
    private static final int IMAGE_R = 52;

    private static final int PHOTO_PADDING_X = 44;
    private static final int PHOTO_PADDING_Y = 40;
    static final int PHOTO_X = IMAGE_X + PHOTO_PADDING_X;
    static final int PHOTO_Y = IMAGE_Y + PHOTO_PADDING_Y;
    static final int PHOTO_W = IMAGE_W - 2 * PHOTO_PADDING_X;
    static final int PHOTO_H = IMAGE_H - 2 * PHOTO_PADDING_Y;

    private static final int FAVORITE_SIZE = 84;
    private static final int FAVORITE_X = IMAGE_X + IMAGE_W - 24 - FAVORITE_SIZE;
    private static final int FAVORITE_Y = IMAGE_Y + 24;

    private static final int CONTENT_X = IMAGE_X;
    private static final int CONTENT_W = IMAGE_W;
    private static final int CONTENT_Y = IMAGE_Y + IMAGE_H + 16;
    private static final int CONTENT_BOTTOM = CARD_Y + CARD_H - CARD_PADDING;
    private static final int CONTENT_PADDING_X = 34;
    private static final int CONTENT_PADDING_TOP = 24;
    private static final int CONTENT_PADDING_BOTTOM = 36;
    private static final int CONTENT_GAP = 8;

    private static final int TITLE_SIZE = 44;
    private static final int TITLE_BLOCK_H = 51;
    private static final int BRAND_SIZE = 30;
    private static final int BRAND_BLOCK_H = 36;

    private static final int CART_SIZE = 130;
    private static final int CART_RADIUS = 44;
    private static final int CART_X = CONTENT_X + CONTENT_W - CONTENT_PADDING_X - CART_SIZE;
    private static final int CART_Y = CONTENT_BOTTOM - CONTENT_PADDING_BOTTOM - CART_SIZE;

    private static final int PRICE_SIZE = 64;
    private static final int PRICE_BLOCK_H = 77;

    private static final int TEXT_X = CONTENT_X + CONTENT_PADDING_X;
    private static final int TEXT_MAX_W = CONTENT_W - 2 * CONTENT_PADDING_X;
    private static final int TITLE_Y = CONTENT_Y + CONTENT_PADDING_TOP;
    private static final int BRAND_Y = TITLE_Y + TITLE_BLOCK_H + CONTENT_GAP;

    private static final Font INTER_REGULAR = loadFont("fonts/Inter-Regular.ttf");
    private static final Font INTER_BOLD = loadFont("fonts/Inter-Bold.ttf");
    private static final Font INTER_EXTRABOLD = loadFont("fonts/Inter-ExtraBold.ttf");

    public SocialImageResponseDto generateSocialImages(List<SocialProductDto> products,
            UUID requesterId) {

        assertOwnership(products, requesterId);

        List<SocialImageItemDto> results = new ArrayList<>();

        for (SocialProductDto product : products) {
            try {

                String cacheKey = cacheKey(product);

                SocialImageCache cached = socialImageCacheRepository.findByCacheKey(cacheKey).orElse(null);

                if (cached != null && storageService.exists(cached.getR2Key())) {
                    logger.info("Cache hit para '{}' (key={}) -> {}",
                            product.title(), cacheKey, cached.getImageUrl());
                    results.add(new SocialImageItemDto(
                            product.title(), cached.getImageUrl(), product.price(), true));
                    continue;
                }

                logger.info("Cache miss para '{}' (key={}) -> gerando nova imagem",
                        product.title(), cacheKey);
                byte[] imageBytes = generateSingleImage(product);
                String r2Key = "social/" + UUID.randomUUID() + ".png";
                String uploadedUrl = storageService.uploadBytes(imageBytes, r2Key, "image/png");

                String finalUrl = persistCache(cached, cacheKey, product, r2Key, uploadedUrl);

                results.add(new SocialImageItemDto(
                        product.title(), finalUrl, product.price(), false));
            } catch (Exception e) {

                logger.error("Erro ao gerar imagem para o produto '{}': {}",
                        product.title(), e.getMessage());
            }
        }

        return new SocialImageResponseDto(results);
    }

    byte[] generateSingleImage(SocialProductDto product) throws IOException {
        return render(product, download(product.imageUrl()));
    }

    byte[] render(SocialProductDto product, BufferedImage photo) throws IOException {
        BufferedImage canvas = new BufferedImage(CANVAS, CANVAS, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setColor(PAGE_BG);
        g.fillRect(0, 0, CANVAS, CANVAS);

        drawShadow(g);

        g.setColor(CARD_BG);
        g.fill(new RoundRectangle2D.Double(CARD_X, CARD_Y, CARD_W, CARD_H, CARD_R, CARD_R));

        g.setColor(SURFACE_LIGHT);
        g.fill(new RoundRectangle2D.Double(IMAGE_X, IMAGE_Y, IMAGE_W, IMAGE_H, IMAGE_R, IMAGE_R));

        if (photo != null) {
            drawContained(g, photo, PHOTO_X, PHOTO_Y, PHOTO_W, PHOTO_H);
        }

        drawFavoriteButton(g);
        drawTitle(g, product.title());
        drawBrand(g, product.brand());
        drawPrice(g, product.price());
        drawCartButton(g);

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(canvas, "png", baos);
        return baos.toByteArray();
    }

    private static void drawContained(Graphics2D g, BufferedImage image, int boxX, int boxY, int boxW, int boxH) {
        double scale = Math.min((double) boxW / image.getWidth(), (double) boxH / image.getHeight());
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        int x = boxX + (boxW - width) / 2;
        int y = boxY + (boxH - height) / 2;

        g.drawImage(image, x, y, width, height, null);
    }

    private static void drawShadow(Graphics2D g) {
        Composite original = g.getComposite();
        for (int i = 14; i >= 1; i--) {
            float alpha = 0.010f + 0.014f * (14 - i) / 14f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int grow = i * 2;
            g.fill(new RoundRectangle2D.Double(CARD_X - grow, CARD_Y - grow + i,
                    CARD_W + 2 * grow, CARD_H + 2 * grow, CARD_R + grow, CARD_R + grow));
        }
        g.setComposite(original);
    }

    private static void drawFavoriteButton(Graphics2D g) {
        g.setColor(CARD_BG);
        g.fill(new RoundRectangle2D.Double(FAVORITE_X, FAVORITE_Y, FAVORITE_SIZE, FAVORITE_SIZE,
                FAVORITE_SIZE, FAVORITE_SIZE));

        g.setColor(new Color(255, 255, 255, 204));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new RoundRectangle2D.Double(FAVORITE_X + 2.5, FAVORITE_Y + 2.5,
                FAVORITE_SIZE - 5, FAVORITE_SIZE - 5, FAVORITE_SIZE, FAVORITE_SIZE));

        int iconSize = 36;
        int iconX = FAVORITE_X + (FAVORITE_SIZE - iconSize) / 2;
        int iconY = FAVORITE_Y + (FAVORITE_SIZE - iconSize) / 2;
        drawHeart(g, iconX, iconY, iconSize, WHITE, 3f);
    }

    private static void drawHeart(Graphics2D g, int x, int y, int size, Color color, float stroke) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(px(x, size, 0.50), py(y, size, 0.92));
        path.curveTo(px(x, size, 0.22), py(y, size, 0.72),
                px(x, size, 0.04), py(y, size, 0.53),
                px(x, size, 0.04), py(y, size, 0.33));
        path.curveTo(px(x, size, 0.04), py(y, size, 0.16),
                px(x, size, 0.17), py(y, size, 0.06),
                px(x, size, 0.30), py(y, size, 0.06));
        path.curveTo(px(x, size, 0.40), py(y, size, 0.06),
                px(x, size, 0.47), py(y, size, 0.12),
                px(x, size, 0.50), py(y, size, 0.20));
        path.curveTo(px(x, size, 0.53), py(y, size, 0.12),
                px(x, size, 0.60), py(y, size, 0.06),
                px(x, size, 0.70), py(y, size, 0.06));
        path.curveTo(px(x, size, 0.83), py(y, size, 0.06),
                px(x, size, 0.96), py(y, size, 0.16),
                px(x, size, 0.96), py(y, size, 0.33));
        path.curveTo(px(x, size, 0.96), py(y, size, 0.53),
                px(x, size, 0.78), py(y, size, 0.72),
                px(x, size, 0.50), py(y, size, 0.92));
        path.closePath();

        Stroke original = g.getStroke();
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color);
        g.draw(path);
        g.setStroke(original);
    }

    private static void drawCartButton(Graphics2D g) {
        g.setColor(WHITE);
        g.fill(new RoundRectangle2D.Double(CART_X, CART_Y, CART_SIZE, CART_SIZE, CART_RADIUS, CART_RADIUS));

        int iconSize = 64;
        int iconX = CART_X + (CART_SIZE - iconSize) / 2;
        int iconY = CART_Y + (CART_SIZE - iconSize) / 2;
        drawBag(g, iconX, iconY, iconSize, CARD_BG, 4.5f);
    }

    private static void drawBag(Graphics2D g, int x, int y, int size, Color color, float stroke) {
        Stroke original = g.getStroke();
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color);

        g.fill(new RoundRectangle2D.Double(px(x, size, 0.12), py(y, size, 0.40),
                size * 0.76, size * 0.52, size * 0.14, size * 0.14));

        g.draw(new Arc2D.Double(px(x, size, 0.33), py(y, size, 0.23),
                size * 0.34, size * 0.34, 0, -180, Arc2D.OPEN));

        g.setStroke(original);
    }

    private void drawTitle(Graphics2D g, String rawTitle) {
        String title = rawTitle == null || rawTitle.isBlank() ? "Produto Natura" : rawTitle.trim();
        Font font = font(INTER_BOLD, Font.BOLD, TITLE_SIZE);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();

        g.setColor(TEXT_LIGHT);
        g.drawString(truncateText(title, metrics, TEXT_MAX_W), TEXT_X, baseline(metrics, TITLE_Y, TITLE_BLOCK_H));
    }

    private void drawBrand(Graphics2D g, String rawBrand) {
        String brand = rawBrand == null || rawBrand.isBlank() ? DEFAULT_BRAND : rawBrand.trim();
        Font font = font(INTER_REGULAR, Font.PLAIN, BRAND_SIZE);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();

        g.setColor(TEXT_MUTED);
        g.drawString(truncateText(brand, metrics, TEXT_MAX_W), TEXT_X, baseline(metrics, BRAND_Y, BRAND_BLOCK_H));
    }

    private void drawPrice(Graphics2D g, Double price) {
        if (price == null) {
            return;
        }

        Font font = font(INTER_EXTRABOLD, Font.BOLD, PRICE_SIZE);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();

        String text = String.format(Locale.of("pt", "BR"), "R$ %.2f", price);
        int blockY = CART_Y + CART_SIZE - PRICE_BLOCK_H;

        g.setColor(TEXT_LIGHT);
        g.drawString(text, TEXT_X, baseline(metrics, blockY, PRICE_BLOCK_H));
    }

    private static int baseline(FontMetrics metrics, int blockTop, int blockHeight) {
        int textHeight = metrics.getAscent() + metrics.getDescent();
        return blockTop + (blockHeight - textHeight) / 2 + metrics.getAscent();
    }

    private static float px(int x, int size, double ratio) {
        return (float) (x + size * ratio);
    }

    private static float py(int y, int size, double ratio) {
        return (float) (y + size * ratio);
    }

    private String truncateText(String text, FontMetrics metrics, int maxWidth) {
        if (metrics.stringWidth(text) <= maxWidth) {
            return text;
        }
        while (metrics.stringWidth(text + "...") > maxWidth && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    private BufferedImage download(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        try {
            URLConnection connection = new URL(imageUrl).openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(15_000);

            try (InputStream in = connection.getInputStream()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                in.transferTo(buffer);
                return ImageIO.read(new ByteArrayInputStream(buffer.toByteArray()));
            }
        } catch (Exception e) {
            logger.warn("Nao foi possivel carregar a imagem do produto: {}", imageUrl);
            return null;
        }
    }

    private static Font font(Font embedded, int fallbackStyle, float size) {
        if (embedded != null) {
            return embedded.deriveFont(size);
        }
        return new Font(Font.SANS_SERIF, fallbackStyle, Math.round(size));
    }

    private static Font loadFont(String path) {
        try (InputStream in = SocialImageService.class.getResourceAsStream("/" + path)) {
            if (in != null) {
                return Font.createFont(Font.TRUETYPE_FONT, in);
            }
            logger.warn("Fonte nao encontrada no classpath: {}", path);
        } catch (Exception e) {
            logger.warn("Falha ao carregar a fonte {}: {}", path, e.getMessage());
        }
        return null;
    }

    private static Color color(String hex) {
        return new Color(
                Integer.parseInt(hex.substring(1, 3), 16),
                Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5, 7), 16));
    }

    private String persistCache(SocialImageCache cached,
            String cacheKey,
            SocialProductDto product,
            String r2Key,
            String uploadedUrl) {

        boolean isNew = cached == null;
        SocialImageCache entity = isNew
                ? SocialImageCache.builder().cacheKey(cacheKey).build()
                : cached;

        entity.setProductId(product.productId());
        entity.setProductTitle(product.title());
        entity.setProductPrice(product.price());
        entity.setProductImageUrl(product.imageUrl());
        entity.setBrand(product.brand());
        entity.setR2Key(r2Key);
        entity.setImageUrl(uploadedUrl);

        try {
            socialImageCacheRepository.save(entity);
            return uploadedUrl;

        } catch (DataIntegrityViolationException ex) {

            if (!isNew) {
                throw ex;
            }

            logger.warn("Corrida de cache na key={}; reaproveitando a imagem vencedora", cacheKey);

            try {
                storageService.deleteFile(uploadedUrl);
            } catch (Exception cleanupEx) {
                logger.warn("Nao foi possivel apagar o arquivo orfao {}: {}",
                        uploadedUrl, cleanupEx.getMessage());
            }

            SocialImageCache winner = socialImageCacheRepository
                    .findByCacheKey(cacheKey)
                    .orElseThrow();
            return winner.getImageUrl();
        }
    }

    private void assertOwnership(List<SocialProductDto> products, UUID requesterId) {
        Set<UUID> ids = products.stream()
                .map(SocialProductDto::productId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return;
        }

        Map<UUID, UUID> ownerByProduct = productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(
                        Products::getId,
                        p -> p.getUser().getId()));

        for (UUID id : ids) {
            UUID owner = ownerByProduct.get(id);
            if (owner == null || !owner.equals(requesterId)) {

                throw new ResourceNotFoundException("Produto não encontrado");
            }
        }
    }

    private static String cacheKey(SocialProductDto product) {
        String payload = String.join("\u001f",
                product.productId() == null ? "" : product.productId().toString(),
                product.title() == null ? "" : product.title(),
                product.price() == null ? "" : Double.toString(product.price()),
                product.imageUrl() == null ? "" : product.imageUrl(),
                product.brand() == null ? "" : product.brand());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
