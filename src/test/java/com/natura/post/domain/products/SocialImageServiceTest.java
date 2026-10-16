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
import com.natura.post.domain.products.SocialImageService.Layout;

class SocialImageServiceTest {

    private static final int PRODUCT_WIDTH = 300;
    private static final int PRODUCT_HEIGHT = 900;

    private static final int SURFACE = new Color(0xFA, 0xF8, 0xF3).getRGB();
    private static final int PAGE_BG = new Color(0xF1, 0xEE, 0xE7).getRGB();
    private static final int MINT = new Color(0xB7, 0xD9, 0xC1).getRGB();
    private static final int FOREST = new Color(0x0F, 0x51, 0x32).getRGB();

    private final SocialImageService service = new SocialImageService(null, null, null);

    @Test
    void deveGerarImagemVertical1080x1920() throws Exception {
        BufferedImage rendered = ImageIO.read(new ByteArrayInputStream(
                service.render(product(), tallProduct())));

        assertEquals(1080, rendered.getWidth(), "largura do story deve ser 1080");
        assertEquals(1920, rendered.getHeight(), "altura do story deve ser 1920 (vertical)");
    }

    @Test
    void devePreservarAProporcaoDaFotoDoProduto() throws Exception {
        Layout layout = service.computeLayout(product());
        BufferedImage rendered = ImageIO.read(new ByteArrayInputStream(
                service.render(product(), tallProduct())));

        Box product = findColor(rendered, Color.RED.getRGB());
        double renderedAspect = (double) product.width / product.height;
        double sourceAspect = (double) PRODUCT_WIDTH / PRODUCT_HEIGHT;

        assertEquals(sourceAspect, renderedAspect, 0.02, "foto distorcida: proporcao original nao preservada");
        assertTrue(product.width <= layout.photoW(), "foto esticada horizontalmente dentro da caixa");
        assertTrue(product.height <= layout.photoH(), "foto esticada verticalmente dentro da caixa");
    }

    @Test
    void deveCentralizarAFotoSemEsticarNaSuperficieClara() throws Exception {
        Layout layout = service.computeLayout(product());
        BufferedImage rendered = ImageIO.read(new ByteArrayInputStream(
                service.render(product(), tallProduct())));

        Box product = findColor(rendered, Color.RED.getRGB());

        int boxX = layout.photoX();
        int boxY = layout.photoY();
        int boxWidth = layout.photoW();
        int boxHeight = layout.photoH();

        assertEquals(SURFACE, rendered.getRGB(boxX + 8, boxY + boxHeight / 2),
                "a lateral da caixa deveria manter a superficie clara (object-fit: contain)");
        assertEquals(SURFACE, rendered.getRGB(boxX + boxWidth - 8, boxY + boxHeight / 2),
                "a lateral da caixa deveria manter a superficie clara (object-fit: contain)");
        assertTrue(product.x > boxX && product.x + product.width < boxX + boxWidth,
                "foto deveria ficar centralizada na caixa");
        assertEquals(Color.RED.getRGB(), rendered.getRGB(boxX + boxWidth / 2, boxY + boxHeight / 2),
                "o centro da caixa deveria conter o produto");
    }

    @Test
    void deveMontarOStoryMesmoSemFoto() throws Exception {
        Layout layout = service.computeLayout(product());
        BufferedImage rendered = ImageIO.read(new ByteArrayInputStream(service.render(product(), null)));

        assertEquals(PAGE_BG, rendered.getRGB(8, 8), "fundo off-white do CALM");
        assertEquals(SURFACE, rendered.getRGB(layout.cardX() + 40, layout.cardY() + 40),
                "card de superficie clara");
        assertEquals(MINT, rendered.getRGB(1000, 60), "circulo decorativo mint no topo");
        assertEquals(FOREST, rendered.getRGB(layout.pillX() + 60, layout.pillY() + layout.pillH() / 2),
                "pill de preco verde forest");
    }

    @Test
    void deveManterOsBlocosDentroDaAreaSeguraDoStory() {
        Layout layout = service.computeLayout(product());

        assertTrue(layout.cardY() > 240, "card comeca abaixo do cabecalho");
        assertTrue(layout.cardY() + layout.cardH() < layout.pillY(), "card nao pode invadir a faixa de preco");
        assertTrue(layout.pillY() + layout.pillH() <= 1690, "preco dentro da zona segura inferior");
        assertTrue(layout.photoW() > 0 && layout.photoH() > 0, "caixa da foto precisa existir");
    }

    private static SocialProductDto product() {
        return new SocialProductDto("Creme Hidratante Corporal", 189.90, "http://localhost/produto.png", "Natura Ekos", null);
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
