package com.natura.post.domain.products;

import java.awt.*;
import java.awt.geom.Ellipse2D;
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

    private static final String TEMPLATE_VERSION = "story-calm-v1";

    static final int CANVAS_W = 1080;
    static final int CANVAS_H = 1920;

    private static final Color PAGE_BG = color("#F1EEE7");
    private static final Color SURFACE = color("#FAF8F3");
    private static final Color FOREST = color("#0F5132");
    private static final Color MINT = color("#B7D9C1");
    private static final Color TEXT_PRIMARY = color("#242B27");
    private static final Color SHADOW = color("#333027");
    private static final Color WHITE = Color.WHITE;

    private static final String DEFAULT_BRAND = "";
    private static final String DEFAULT_TITLE = "Produto Natura";

    static final int CONTENT_X = 80;
    static final int CONTENT_W = CANVAS_W - 2 * CONTENT_X;

    static final int HEADER_TOP = 240;
    static final int HEADER_GAP = 24;
    static final int RULE_W = 96;
    static final int RULE_H = 4;

    static final int OVERLINE_SIZE = 28;
    static final int OVERLINE_TRACK = 7;

    static final int TITLE_SIZE = 124;
    static final int TITLE_SIZE_MIN = 84;
    static final int TITLE_MAX_LINES = 3;
    static final double TITLE_LINE_RATIO = 1.08;

    static final int CARD_MAX_H = 820;
    static final int CARD_MIN_H = 560;
    static final int CARD_R = 24;
    static final int PHOTO_PAD_X = 60;
    static final int PHOTO_PAD_Y = 55;

    static final int PILL_W = 640;
    static final int PILL_H = 170;
    static final int PILL_X = CONTENT_X + (CONTENT_W - PILL_W) / 2;
    static final int PRICE_TOP = 1520;
    static final int PRICE_SIZE = 76;
    static final int PRICE_TRACK = -1;

    static final int DECOR_CX = 1050;
    static final int DECOR_CY = 30;
    static final int DECOR_R = 190;

    private static final int CONTENT_BOTTOM = 1690;

    private static final Font INTER_REGULAR = loadFont("fonts/Inter-Regular.ttf");
    private static final Font INTER_EXTRABOLD = loadFont("fonts/Inter-ExtraBold.ttf");
    private static final Font DISPLAY_REGULAR = loadFont("fonts/CormorantGaramond.ttf");

    record Layout(
            int cardX,
            int cardY,
            int cardW,
            int cardH,
            int photoX,
            int photoY,
            int photoW,
            int photoH,
            int pillX,
            int pillY,
            int pillW,
            int pillH,
            int titleSize,
            int titleLineH,
            int overlineH,
            List<String> titleLines) {
    }

    public SocialImageResponseDto generateSocialImages(List<SocialProductDto> products,
            UUID requesterId) {

        assertProductIdsPresent(products);
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

    Layout computeLayout(SocialProductDto product) {
        BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scratch.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            String title = title(product);

            g.setFont(font(INTER_REGULAR, Font.PLAIN, OVERLINE_SIZE));
            FontMetrics overlineMetrics = g.getFontMetrics();
            int overlineH = overlineMetrics.getAscent() + overlineMetrics.getDescent();

            int titleSize = TITLE_SIZE;
            List<String> lines;
            FontMetrics titleMetrics;
            while (true) {
                g.setFont(font(DISPLAY_REGULAR, Font.PLAIN, titleSize));
                titleMetrics = g.getFontMetrics();
                lines = wrap(title, titleMetrics, CONTENT_W);
                if (lines.size() <= 2 || titleSize <= TITLE_SIZE_MIN) {
                    break;
                }
                titleSize -= 8;
            }
            lines = limitLines(lines, titleMetrics, CONTENT_W, TITLE_MAX_LINES);

            int titleLineH = (int) Math.ceil(titleSize * TITLE_LINE_RATIO);
            int headerH = RULE_H + HEADER_GAP + overlineH + HEADER_GAP + titleLineH * lines.size();

            boolean hasPrice = product.price() != null;
            int pillY = hasPrice ? PRICE_TOP : CONTENT_BOTTOM;
            int space = pillY - (HEADER_TOP + headerH);

            int gap = 56;
            int cardH = space - 2 * gap;
            if (cardH > CARD_MAX_H) {
                cardH = CARD_MAX_H;
                gap = (space - cardH) / 2;
            } else if (cardH < CARD_MIN_H) {
                cardH = CARD_MIN_H;
                gap = Math.max(16, (space - cardH) / 2);
            }
            int cardY = HEADER_TOP + headerH + gap;

            int cardX = CONTENT_X;
            int cardW = CONTENT_W;
            int photoX = cardX + PHOTO_PAD_X;
            int photoY = cardY + PHOTO_PAD_Y;
            int photoW = cardW - 2 * PHOTO_PAD_X;
            int photoH = cardH - 2 * PHOTO_PAD_Y;

            return new Layout(cardX, cardY, cardW, cardH,
                    photoX, photoY, photoW, photoH,
                    PILL_X, pillY, PILL_W, hasPrice ? PILL_H : 0,
                    titleSize, titleLineH, overlineH, lines);
        } finally {
            g.dispose();
        }
    }

    byte[] render(SocialProductDto product, BufferedImage photo) throws IOException {
        Layout layout = computeLayout(product);

        BufferedImage canvas = new BufferedImage(CANVAS_W, CANVAS_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g.setColor(PAGE_BG);
            g.fillRect(0, 0, CANVAS_W, CANVAS_H);

            drawDecoration(g);
            drawCard(g, layout);

            if (photo != null) {
                drawContained(g, photo, layout.photoX(), layout.photoY(), layout.photoW(), layout.photoH());
            }

            drawHeader(g, product, layout);
            drawPrice(g, product, layout);
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(canvas, "png", baos);
        return baos.toByteArray();
    }

    private static void drawDecoration(Graphics2D g) {
        g.setColor(MINT);
        g.fill(new Ellipse2D.Double(DECOR_CX - DECOR_R, DECOR_CY - DECOR_R,
                DECOR_R * 2.0, DECOR_R * 2.0));
    }

    private static void drawCard(Graphics2D g, Layout layout) {
        drawSoftShadow(g, layout.cardX(), layout.cardY(), layout.cardW(), layout.cardH(),
                CARD_R, 12, 1, 10, 0.12);
        g.setColor(SURFACE);
        g.fill(new RoundRectangle2D.Double(layout.cardX(), layout.cardY(),
                layout.cardW(), layout.cardH(), CARD_R, CARD_R));
    }

    private static void drawSoftShadow(Graphics2D g, double x, double y, double w, double h,
            double radius, int layers, double growStep, double offsetY, double targetAlpha) {

        Composite original = g.getComposite();
        double perLayer = 1.0 - Math.pow(1.0 - targetAlpha, 1.0 / layers);
        float alpha = (float) perLayer;

        g.setColor(SHADOW);
        for (int i = layers; i >= 1; i--) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            double grow = i * growStep;
            g.fill(new RoundRectangle2D.Double(x - grow, y - grow + offsetY,
                    w + 2 * grow, h + 2 * grow, radius + grow, radius + grow));
        }
        g.setComposite(original);
    }

    private void drawHeader(Graphics2D g, SocialProductDto product, Layout layout) {
        g.setColor(FOREST);
        g.fill(new RoundRectangle2D.Double(CONTENT_X, HEADER_TOP, RULE_W, RULE_H, RULE_H, RULE_H));

        int y = HEADER_TOP + RULE_H + HEADER_GAP;

        g.setFont(font(INTER_REGULAR, Font.PLAIN, OVERLINE_SIZE));
        FontMetrics overlineMetrics = g.getFontMetrics();
        String overline = truncateTracked(overline(product), overlineMetrics, CONTENT_W, OVERLINE_TRACK);
        g.setColor(FOREST);
        drawTracked(g, overline, CONTENT_X, y + overlineMetrics.getAscent(), OVERLINE_TRACK);

        y += layout.overlineH() + HEADER_GAP;

        g.setFont(font(DISPLAY_REGULAR, Font.PLAIN, layout.titleSize()));
        FontMetrics titleMetrics = g.getFontMetrics();
        g.setColor(TEXT_PRIMARY);

        List<String> lines = layout.titleLines();
        for (int i = 0; i < lines.size(); i++) {
            int lineTop = y + i * layout.titleLineH();
            g.drawString(lines.get(i), CONTENT_X, baseline(titleMetrics, lineTop, layout.titleLineH()));
        }
    }

    private void drawPrice(Graphics2D g, SocialProductDto product, Layout layout) {
        if (product.price() == null || layout.pillH() == 0) {
            return;
        }

        drawSoftShadow(g, layout.pillX(), layout.pillY(), layout.pillW(), layout.pillH(),
                layout.pillH() / 2.0, 6, 1, 4, 0.10);

        g.setColor(FOREST);
        g.fill(new RoundRectangle2D.Double(layout.pillX(), layout.pillY(),
                layout.pillW(), layout.pillH(), layout.pillH(), layout.pillH()));

        Font font = font(INTER_EXTRABOLD, Font.BOLD, PRICE_SIZE);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();

        String text = String.format(Locale.of("pt", "BR"), "R$ %.2f", product.price());
        int textWidth = trackedWidth(text, metrics, PRICE_TRACK);
        int x = layout.pillX() + (layout.pillW() - textWidth) / 2;
        int blockTop = layout.pillY();

        g.setColor(WHITE);
        drawTracked(g, text, x, baseline(metrics, blockTop, layout.pillH()), PRICE_TRACK);
    }

    private static void drawContained(Graphics2D g, BufferedImage image, int boxX, int boxY, int boxW, int boxH) {
        double scale = Math.min((double) boxW / image.getWidth(), (double) boxH / image.getHeight());
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        int x = boxX + (boxW - width) / 2;
        int y = boxY + (boxH - height) / 2;

        g.drawImage(image, x, y, width, height, null);
    }

    private static int baseline(FontMetrics metrics, int blockTop, int blockHeight) {
        int textHeight = metrics.getAscent() + metrics.getDescent();
        return blockTop + (blockHeight - textHeight) / 2 + metrics.getAscent();
    }

    private static int trackedWidth(String text, FontMetrics metrics, int track) {
        if (text.isEmpty()) {
            return 0;
        }
        return metrics.stringWidth(text) + track * (text.length() - 1);
    }

    private static void drawTracked(Graphics2D g, String text, int x, int baseline, int track) {
        FontMetrics metrics = g.getFontMetrics();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            g.drawString(String.valueOf(c), x, baseline);
            x += metrics.charWidth(c) + track;
        }
    }

    private static String truncateTracked(String text, FontMetrics metrics, int maxWidth, int track) {
        if (trackedWidth(text, metrics, track) <= maxWidth) {
            return text;
        }
        while (!text.isEmpty() && trackedWidth(text + "...", metrics, track) > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    private static List<String> wrap(String text, FontMetrics metrics, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        for (String word : text.trim().split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (line.isEmpty()) {
                if (metrics.stringWidth(word) > maxWidth) {
                    hardBreak(word, metrics, maxWidth, lines);
                    continue;
                }
                line.append(word);
            } else if (metrics.stringWidth(line.toString() + " " + word) <= maxWidth) {
                line.append(' ').append(word);
            } else {
                lines.add(line.toString());
                line.setLength(0);
                if (metrics.stringWidth(word) > maxWidth) {
                    hardBreak(word, metrics, maxWidth, lines);
                    continue;
                }
                line.append(word);
            }
        }

        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        if (lines.isEmpty()) {
            lines.add(text.trim());
        }
        return lines;
    }

    private static void hardBreak(String word, FontMetrics metrics, int maxWidth, List<String> lines) {
        String rest = word;
        while (!rest.isEmpty() && metrics.stringWidth(rest) > maxWidth) {
            int cut = 1;
            while (cut < rest.length() && metrics.stringWidth(rest.substring(0, cut + 1)) <= maxWidth) {
                cut++;
            }
            lines.add(rest.substring(0, cut));
            rest = rest.substring(cut);
        }
        if (!rest.isEmpty()) {
            lines.add(rest);
        }
    }

    private static List<String> limitLines(List<String> lines, FontMetrics metrics, int maxWidth, int max) {
        if (lines.size() <= max) {
            return lines;
        }
        List<String> limited = new ArrayList<>(lines.subList(0, max));
        String last = limited.get(max - 1);
        while (!last.isEmpty() && metrics.stringWidth(last + "...") > maxWidth) {
            last = last.substring(0, last.length() - 1);
        }
        limited.set(max - 1, last + "...");
        return limited;
    }

    private static String title(SocialProductDto product) {
        String title = product.title();
        return title == null || title.isBlank() ? DEFAULT_TITLE : title.trim();
    }

    private static String overline(SocialProductDto product) {
        String brand = product.brand();
        String value = brand == null || brand.isBlank() ? DEFAULT_BRAND : brand.trim();
        return value.toUpperCase(Locale.ROOT);
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
        return new Font(Font.SERIF, fallbackStyle, Math.round(size));
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

    private void assertProductIdsPresent(List<SocialProductDto> products) {
        for (SocialProductDto product : products) {
            if (product.productId() == null) {
                throw new IllegalArgumentException("O productId e obrigatorio");
            }
        }
    }

    private void assertOwnership(List<SocialProductDto> products, UUID requesterId) {
        Set<UUID> ids = products.stream()
                .map(SocialProductDto::productId)
                .collect(Collectors.toSet());

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
        String payload = String.join("\\u001f",
                TEMPLATE_VERSION,
                product.productId().toString(),
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
