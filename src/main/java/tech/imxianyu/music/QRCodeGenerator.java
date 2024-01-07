package tech.imxianyu.music;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import tech.imxianyu.rendering.multithreading.AsyncGLContentLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;


public class QRCodeGenerator {

    public static final ResourceLocation qrCode = new ResourceLocation("Zephyr/textures/QRCode.png");

    @SneakyThrows
    public static void generateAndLoadTexture(String address) {
/*
        if (true) {
            browse(address);
            return;
        }*/

        BufferedImage img = QRCodeGenerator.generateQRCode(address, 128, 128);

        Minecraft.getMinecraft().addScheduledTask(() -> {
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

            if (textureManager.getTexture(qrCode) != null) {
                textureManager.deleteTexture(qrCode);
            }

            textureManager.loadTexture(qrCode, new DynamicTexture(img));
        });

    }

    @SneakyThrows
    public static void browse(String address) {
        BufferedImage img = QRCodeGenerator.generateQRCode(address, 128, 128);

        ImageIO.write(img, "png", new File("H:\\cm.png"));

        Desktop.getDesktop().browse(new File("H:\\cm.png").toURI());
    }


    /**
     * 根据内容，生成指定宽高、指定格式的二维码图片
     *
     * @param text   内容
     * @param width  宽
     * @param height 高
     * @return 生成的二维码图片文件对象
     */
    public static BufferedImage generateQRCode(String text, int width, int height) throws Exception {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * 用于二维码的生成，由Google提供。
     * <p>
     * Created by Eric on 2017/2/15.
     */
    public static final class MatrixToImageWriter {

        private static final int BLACK = 0xFF000000;
        private static final int WHITE = 0xFFFFFFFF;

        private MatrixToImageWriter() {
        }

        public static BufferedImage toBufferedImage(BitMatrix matrix) {
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
                }
            }
            return image;
        }

        public static void writeToFile(BitMatrix matrix, String format, File file)
                throws IOException {
            BufferedImage image = toBufferedImage(matrix);
            if (!ImageIO.write(image, format, file)) {
                throw new IOException("Could not write an image of format " + format + " to " + file);
            }
        }

    }

}