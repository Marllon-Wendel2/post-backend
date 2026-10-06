package com.natura.post.domain.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import com.natura.post.domain.products.dtos.SocialImage.SocialProductDto;

class SocialImageServiceTest {

    private static final int PRODUCT_WIDTH = 300;
    private static final int PRODUCT_HEIGHT = 900;

    private static final int SURFACE_LIGHT = new Color(0xF2, 0xF2, 0xF2).getRGB();
    private static final int PAGE_BG = new Color(0xEE, 0xEE, 0xEC).getRGB();

    private final SocialImageService service = new SocialImageService(null, null, null);

    @Test
    void devePreservarAProporcaoDaFotoDoProduto() throws Exception {
        BufferedImage rendered = ImageIO.read(new ByteArrayInputStream(
                service.render(product(), tallProduct())));

        Box product = findColor(rendered, Color.RED.getRGB());
        double renderedAspect = (double) product.width / product.height;
        double sourceAspect = (double) PRODUCT_WIDTH / PRODUCT_HEIGHT;

        assertEquals(sourceAspect, renderedAspect, 0.02, "foto distorcida: proporcao original nao preservada");
        assertTrue(product.width <= SocialImageService.PHOTO_W, "foto esticada horizontalmente dentro da caixa");
        assertTrue(product.height <= SocialImageService.PHOTO_H, "foto esticada verticalmente dentro da caixa");
    }

    @Test
    void deveCentralizarAFotoSemEsticarNaSuperficieClara() throws Exception {
        BufferedImage rendered = ImageIO.read(new ByteArrayInputStream(
                service.render(product(), tallProduct())));

        Box product = findColor(rendered, Color.RED.getRGB());

        int boxX = SocialImageService.PHOTO_X;
        int boxY = SocialImageService.PHOTO_Y;
        int boxWidth = SocialImageService.PHOTO_W;
        int boxHeight = SocialImageService.PHOTO_H;

        assertEquals(SURFACE_LIGHT, rendered.getRGB(boxX + 8, boxY + boxHeight / 2),
                "a lateral da caixa deveria manter a superficie clara (object-fit: contain)");
        assertEquals(SURFACE_LIGHT, rendered.getRGB(boxX + boxWidth - 8, boxY + boxHeight / 2),
                "a lateral da caixa deveria manter a superficie clara (object-fit: contain)");
        assertTrue(product.x > boxX && product.x + product.width < boxX + boxWidth,
                "foto deveria ficar centralizada na caixa");
        assertEquals(Color.RED.getRGB(), rendered.getRGB(boxX + boxWidth / 2, boxY + boxHeight / 2),
                "o centro da caixa deveria conter o produto");
    }

    @Test
    void deveMontarOCardMesmoSemFoto() throws Exception {
        BufferedImage rendered = ImageIO.read(new ByteArrayInputStream(service.render(product(), null)));

        assertEquals(PAGE_BG, rendered.getRGB(8, 8), "fundo da pagina off-white");
        assertEquals(SURFACE_LIGHT, rendered.getRGB(170, 373), "superficie clara da imagem");
        assertEquals(new Color(0x29, 0x29, 0x29).getRGB(), rendered.getRGB(540, 700), "card grafite");
    }

    private static SocialProductDto product() {
        return new SocialProductDto("Creme Hidratante Corporal", 189.90, "http://localhost/produto.png", null, null);
    }

    private static BufferedImage tallProduct() {
        BufferedImage image = new BufferedImage(PRODUCT_WIDTH, PRODUCT_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, PRODUCT_WIDTH, PRODUCT_HEIGHT);
        g.dispose();
        return image;
    }

    private static Box findColor(BufferedImage image, int rgb) {
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (isRed(image.getRGB(x, y))) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        assertTrue(maxX >= 0, "foto nao foi desenhada na imagem gerada");
        return new Box(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private static boolean isRed(int rgb) {
        return ((rgb >> 16) & 0xFF) > 200 && ((rgb >> 8) & 0xFF) < 60 && (rgb & 0xFF) < 60;
    }

    private record Box(int x, int y, int width, int height) {
    }
}
