package tech.imxianyu.rendering.font;

import lombok.SneakyThrows;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.rendering.multithreading.AsyncGLContentLoader;
import tech.imxianyu.utils.debugging.Profiler;
import tech.imxianyu.utils.multithreading.MultiThreadingUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class CFont {
    private final boolean chinese;
    protected HashMap<Character, Integer> charmap = new HashMap();
    protected CharData[] charData = new CharData[256];
    protected Font font;
    protected boolean antiAlias;
    protected boolean fractionalMetrics;
    protected int fontHeight = -1;
    protected int charOffset = 0;
    protected int texID;
    private float imgSize = 512;
    private boolean hasDone;

    public CFont(Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        this.chinese = false;
        this.hasDone = false;
        setupTexture(font, antiAlias, fractionalMetrics, this.charData);
    }

    public CFont(Font font, boolean antiAlias, boolean fractionalMetrics, boolean chinese) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        this.chinese = chinese;
        this.hasDone = false;
        if (this.chinese) {
            int size = font.getSize();

            this.imgSize = 1024 * (size / 10 + 1);

            this.charData = new CharData[8000];
        }
        setupTexture(font, antiAlias, fractionalMetrics, this.charData);
    }

    protected void setupTexture(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        MultiThreadingUtil.runAsync(() -> {
            BufferedImage img = generateFontImage(font, antiAlias, fractionalMetrics, chars);

            AsyncGLContentLoader.loadGLContentAsync(() -> {
                Profiler.start(font.getFontName() + ", " + font.getSize());

                DynamicTexture dynamicTexture = new DynamicTexture(img);
                this.texID = dynamicTexture.getGlTextureId();
                Profiler.endTask();

//                System.out.println("Font " + font.getFontName() + " size " + font.getSize() + " loaded. (" + Profiler.getTaskUsedTime() + "ms)");

            });

        });
    }

    protected BufferedImage generateFontImage(Font font, boolean antiAlias, boolean fractionalMetrics,
                                              CharData[] chars) {
        int imgSize = (int) this.imgSize;
        BufferedImage bufferedImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        g.setFont(font);
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, imgSize, imgSize);
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON
                        : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        FontMetrics fontMetrics = g.getFontMetrics();
        int charHeight = 0;
        int positionX = 0;
        int positionY = 1;

        char[] asciis = CharUtils.getAsciis();

        for (char ch : asciis) {
            CharData charData = new CharData();
            Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(ch), g);
            charData.width = (dimensions.getBounds().width + 8);
            charData.height = dimensions.getBounds().height;

            if (positionX + charData.width >= imgSize) {
                positionX = 0;
                positionY += charHeight;
                charHeight = 0;
            }

            if (charData.height > charHeight) {
                charHeight = charData.height;
            }

            charData.storedX = positionX;
            charData.storedY = positionY;

            if (charData.height > this.fontHeight) {
                this.fontHeight = charData.height;
            }

            chars[ch] = charData;
            g.drawString(String.valueOf(ch), positionX + 2, positionY + fontMetrics.getAscent());
            positionX += charData.width;
        }

        if (this.chinese && !hasDone) {
            char[] chinesechar = CharUtils.getUnicodes();

            int index = 257;
            for (char c : chinesechar) {
                charmap.put(c, index);

                CharData charData = new CharData();
                Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(c), g);
                charData.width = (dimensions.getBounds().width + 8);
                charData.height = dimensions.getBounds().height;

                if (positionX + charData.width >= imgSize) {
                    positionX = 0;
                    positionY += charHeight;
                    charHeight = 0;
                }

                if (charData.height > charHeight) {
                    charHeight = charData.height;
                }

                charData.storedX = positionX;
                charData.storedY = positionY;

                if (charData.height > this.fontHeight) {
                    this.fontHeight = charData.height;
                }

                chars[index] = charData;
                g.drawString(String.valueOf(c), positionX + 2, positionY + fontMetrics.getAscent());
                positionX += charData.width;
                index++;
            }

            /*for (int i = 256; i < 255 + chinesechar.length; i++) {

                charmap.put(chinesechar[i - 256], (char) (i - 256));

                CharData charData = new CharData();
                Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(chinesechar[i - 256]), g);
                charData.width = (dimensions.getBounds().width + 8);
                charData.height = dimensions.getBounds().height;

                if (positionX + charData.width >= imgSize) {
                    positionX = 0;
                    positionY += charHeight;
                    charHeight = 0;
                }

                if (charData.height > charHeight) {
                    charHeight = charData.height;
                }

                charData.storedX = positionX;
                charData.storedY = positionY;

                if (charData.height > this.fontHeight) {
                    this.fontHeight = charData.height;
                }

                chars[i] = charData;
                g.drawString(String.valueOf(chinesechar[i - 256]), positionX + 2, positionY + fontMetrics.getAscent());
                positionX += charData.width;
            }*/
            hasDone = true;
        }

        return bufferedImage;
    }

    public void drawChar(CharData[] chars, int c, float x, float y) throws ArrayIndexOutOfBoundsException {
        try {
            drawQuad(x, y, chars[c].width, chars[c].height, chars[c].storedX, chars[c].storedY, chars[c].width,
                    chars[c].height);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void drawQuad(float x, float y, float width, float height, float srcX, float srcY, float srcWidth,
                            float srcHeight) {
        float renderSRCX = srcX / imgSize;
        float renderSRCY = srcY / imgSize;
        float renderSRCWidth = srcWidth / imgSize;
        float renderSRCHeight = srcHeight / imgSize;
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2f(renderSRCX, renderSRCY);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        GL11.glVertex2d(x + width, y);
    }

    public int getStringHeight(String text) {
        return getHeight() * (text + "\n").split("\n").length;
    }

    public int getHeight() {
        return (this.fontHeight - 8) / 2;
    }

    public int getStringWidth(String text) {
        int width = 0;

        for (char c : text.toCharArray()) {
            if ((c < this.charData.length) && (c >= 0)) {
                width += this.charData[c].width - 8 + this.charOffset;
            }
        }

        return width / 2;
    }

    public boolean isAntiAlias() {
        return this.antiAlias;
    }

    public void setAntiAlias(boolean antiAlias) {
        if (this.antiAlias != antiAlias) {
            this.antiAlias = antiAlias;
            setupTexture(this.font, antiAlias, this.fractionalMetrics, this.charData);
        }
    }

    public boolean isFractionalMetrics() {
        return this.fractionalMetrics;
    }

    public void setFractionalMetrics(boolean fractionalMetrics) {
        if (this.fractionalMetrics != fractionalMetrics) {
            this.fractionalMetrics = fractionalMetrics;
            setupTexture(this.font, this.antiAlias, fractionalMetrics, this.charData);
        }
    }

    public Font getFont() {
        return this.font;
    }

    public void setFont(Font font) {
        this.font = font;
        setupTexture(font, this.antiAlias, this.fractionalMetrics, this.charData);
    }

    public interface CallBack {

        void onCallBack(int texId, CharData[] dumbAssShit);

    }

    public class CharData {
        public int width;
        public int height;
        public int storedX;
        public int storedY;

        protected CharData() {
        }
    }
}