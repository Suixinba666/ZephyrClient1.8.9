package tech.imxianyu.rendering.font;

import lombok.SneakyThrows;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.Zephyr;
import tech.imxianyu.eventapi.EventBus;
import tech.imxianyu.events.rendering.TextRenderEvent;
import tech.imxianyu.interfaces.IFontRenderer;
import tech.imxianyu.rendering.TexturedShadow;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.utils.dev.DevUtils;
import tech.imxianyu.utils.information.Version;
import tech.imxianyu.utils.other.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ZFontRenderer extends CFont implements IFontRenderer {
    private final int[] colorCode = new int[32];
    private final String colorcodeIdentifiers = "0123456789abcdefklmnor";
    private final double offsetX;
    private final double offsetY;
    private final boolean shouldRender = true;
    private final Random random = new Random();

    public ZFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics, double offsetX, double offsetY) {
        super(font, antiAlias, fractionalMetrics);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        setupMinecraftColorcodes();
    }

    public ZFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics, boolean chinese, double offsetX,
                         double offsetY) {
        super(font, antiAlias, fractionalMetrics, chinese);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        setupMinecraftColorcodes();
    }

    public ZFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        super(font, antiAlias, fractionalMetrics);
        this.offsetX = 0;
        this.offsetY = 0;
        setupMinecraftColorcodes();
    }

    public int drawStringWithShadow(String text, double x, double y, int color) {
        float shadowWidth = drawString(text, x + 0.5D, y + 0.5D, color, true);
        return (int) Math.max(shadowWidth, drawString(text, x, y, color, false));
    }

    public float drawStringWithBetterShadow(String text, double x, double y, int color) {
        TexturedShadow.drawFontShadow(x, y, this.getStringWidth(text) + 1, this.getHeight() - 1, (color >> 24 & 255) * 0.003921568627451F);
        return drawString(text, x, y, color);
    }

    public int drawString(String text, double x, double y, int color) {
        return (int) drawString(text, x, y, color, false);
    }

    public float drawOutlineString(String text, double x, double y, int color, int outlineColor) {
        String outlinetext = StringUtils.removeFormattingCodes(text);
        drawString(outlinetext, x + 0.5, y, outlineColor, false);
        drawString(outlinetext, x - 0.5, y, outlineColor, false);
        drawString(outlinetext, x, y + 0.5, outlineColor, false);
        drawString(outlinetext, x, y - 0.5, outlineColor, false);
        return drawString(text, x, y, color, false);
    }

    public float drawOutlineCenteredString(String text, double x, double y, int color, int onlineColor) {
        return drawOutlineString(text, x - getStringWidth(text) / 2, y, color, onlineColor);
    }

    public float drawCenteredString(String text, double x, double y, int color) {
        return drawString(text, x - getStringWidth(text) / 2, y, color);
    }

    public float drawCenteredStringWithShadow(String text, double x, double y, int color) {
        float shadowWidth = drawString(text, x - getStringWidth(text) / 2 + 0.6D, y + 0.6D, color, true);
        return drawString(text, x - getStringWidth(text) / 2, y, color);
    }

    public float drawString(String text, double x, double y, int color, boolean shadow) {

        TextRenderEvent textRenderEvent = EventBus.call(new TextRenderEvent(text));

        if (textRenderEvent.isCancelled())
            return 0.0f;

        text = textRenderEvent.getText();
        x -= 1;
        x += this.offsetX;
        y += this.offsetY;

        if (text == null) {
            return 0.0F;
        }
        float alpha = (color >> 24 & 0xFF) * 0.003921568627451F;

        CharData[] currentData = this.charData;

        boolean randomCase = false;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;

        x *= 2.0D;
        y = (y - 3.0D) * 2.0D;
        double startX = x;
        double startY = y;
        if (shouldRender) {
            GL11.glPushMatrix();
            GlStateManager.scale(0.5D, 0.5D, 0.5D);
            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            RenderSystem.color(RenderSystem.hexColor((color >> 16 & 0xFF), (color >> 8 & 0xFF),
                    (color & 0xFF), color >> 24 & 0xFF));
            int size = text.length();
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(texID);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);

            for (int i = 0; i < size; i++) {
                char character = text.charAt(i);
                CharData currentDatum;
                int index = character;

                if (character == '§') {
                    int colorIndex = 21;

                    try {
                        colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                    } catch (Exception e) {
                        System.out.println("OOB: " + text.replaceAll("\n", ""));
                    }

                    if (colorIndex < 16) {
                        bold = false;
                        italic = false;
                        randomCase = false;
                        underline = false;
                        strikethrough = false;
                        GlStateManager.bindTexture(texID);
                        currentData = this.charData;

                        if (colorIndex < 0) {
                            colorIndex = 15;
                        }

                        if (shadow) {
                            colorIndex += 16;
                        }

                        int colorcode = this.colorCode[colorIndex];

                        if (text.charAt(i + 1) == 'e') {
                            colorcode = RenderSystem.hexColor(194, 255, 39);
                        }

                        RenderSystem.color(RenderSystem.hexColor((colorcode >> 16 & 0xFF), (colorcode >> 8 & 0xFF),
                                (colorcode & 0xFF), color >> 24 & 0xFF));
                    } else if (colorIndex == 16) {
                        randomCase = true;
                    } else if (colorIndex == 17) {
                        bold = true;
                    } else if (colorIndex == 18) {
                        strikethrough = true;
                    } else if (colorIndex == 19) {
                        underline = true;
                    } else if (colorIndex == 20) {
                    } else {
                        bold = false;
                        italic = false;
                        randomCase = false;
                        underline = false;
                        strikethrough = false;
                        RenderSystem.color(RenderSystem.hexColor((color >> 16 & 0xFF), (color >> 8 & 0xFF),
                                (color & 0xFF), color >> 24 & 0xFF));
                        GlStateManager.bindTexture(texID);
                        currentData = this.charData;
                    }

                    i++;
                } else if (character == '\n') {
                    x = startX;
                    y += this.getHeight() * 2 + 2;
                }else {

                    if (character >= 256) {

                        Integer integer = this.charmap.get(character);

                        if (integer == null) {
                            integer = this.charmap.get('□');

                            if (Zephyr.getInstance().getVersion().getType() == Version.VersionType.Dev && !DevUtils.missingCharacters.contains(character))
                                DevUtils.addCharacter(character);
                        }


                        index = integer;
                        currentDatum = currentData[index];

                    } else {
                        currentDatum = currentData[character];
                    }

                    GL11.glBegin(GL11.GL_TRIANGLES);
                    if (randomCase) {
                        index = CharUtils.lAn.charAt(ThreadLocalRandom.current().nextInt(CharUtils.lAn.toCharArray().length));
                    }

                    drawChar(currentData, index, (float) x, (float) y);
                    GL11.glEnd();

                    if (bold) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        drawChar(currentData, index, (float) x + 0.25f, (float) y);
                        GL11.glEnd();

                        GL11.glBegin(GL11.GL_TRIANGLES);
                        drawChar(currentData, index, (float) x - 0.25f, (float) y);
                        GL11.glEnd();

                        GL11.glBegin(GL11.GL_TRIANGLES);
                        drawChar(currentData, index, (float) x, (float) y + 0.25f);
                        GL11.glEnd();

                        GL11.glBegin(GL11.GL_TRIANGLES);
                        drawChar(currentData, index, (float) x, (float) y - 0.25f);
                        GL11.glEnd();
                    }

                    if (strikethrough) {
                        drawLine(x, y + currentDatum.height / 2.0, x + currentDatum.width - 8.0D,
                                y + currentDatum.height / 2.0, 1.0F);
                    }

                    if (underline) {
                        drawLine(x, y + currentDatum.height - 2.0D, x + currentDatum.width - 8.0D,
                                y + currentDatum.height - 2.0D, 1.0F);
                    }

                    if (character == ' ') {
                        x += currentDatum.width - 8 + this.charOffset;
                    } else {
                        x += currentDatum.width - 8 + this.charOffset;
                    }
                }
            }

            GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE);
            GL11.glPopMatrix();
        }

        return (float) x / 2.0F;
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        }

        TextRenderEvent textRenderEvent = EventBus.call(new TextRenderEvent(text));

        if (textRenderEvent.isCancelled())
            return 0;

        text = textRenderEvent.getText();

        int width = 0;
        CharData[] currentData = this.charData;
        boolean bold = false;
        boolean italic = false;
        int size = text.length();


        for (int i = 0; i < size; i++) {
            char character = text.charAt(i);

            CharData currentDatum;
            int index;

            if ((character == '§') && (i + 1 < size)) {
                int colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));

                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                } else if (colorIndex == 17) {
                    bold = true;
                } else if (colorIndex == 20) {
                    italic = true;
                } else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    currentData = this.charData;
                }

                i++;
            } else {

                if (character >= 256) {

                    Integer integer = this.charmap.get(character);

                    if (integer == null) {
                        integer = this.charmap.get('□');

                        if (Zephyr.getInstance().getVersion().getType() == Version.VersionType.Dev && !DevUtils.missingCharacters.contains(character))
                            DevUtils.addCharacter(character);
                    }


                    index = integer;

                    if (index > currentData.length) {
                        index = this.charmap.get('□');

                        if (Zephyr.getInstance().getVersion().getType() == Version.VersionType.Dev && !DevUtils.missingCharacters.contains(character))
                            DevUtils.addCharacter(character);
                    }

                    try {
                        currentDatum = currentData[index];
                    } catch (Exception e) {
                        currentDatum = currentData[' '];
                    }
                } else {
                    currentDatum = currentData[character];
                }

                width += currentDatum.width - 8 + this.charOffset;
            }
        }

        return width / 2;
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
    }

    @Override
    public void setAntiAlias(boolean antiAlias) {
        super.setAntiAlias(antiAlias);
        
    }

    @Override
    public void setFractionalMetrics(boolean fractionalMetrics) {
        super.setFractionalMetrics(fractionalMetrics);
        
    }


    private void drawLine(double x, double y, double x1, double y1, float width) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public List<String> wrapWords(String text, double width) {
        List<String> finalWords = new ArrayList<>();

        if (getStringWidth(text) > width) {
            List<String> words = new ArrayList<>();

            for (String s : text.split(" ")) {

//                if (s.isEmpty())
//                    continue;
//
//                if (s.contains("\n")) {
//                    Collections.addAll(words, s.split("\n"));
//                } else {
                    words.add(s);
//                }
            }

            StringBuilder currentWord = new StringBuilder();
            StringBuilder colorCodes = new StringBuilder();

            for (String word : words) {

                word = word.trim();

//                int lastIndexOf = word.lastIndexOf("§");
//
//                if (lastIndexOf != -1 && lastIndexOf < word.length() - 1) {
//                    lastColorCode = word.toCharArray()[lastIndexOf + 1];
//                }

                for (int i = 0; i < word.toCharArray().length; i++) {
                    char c = word.toCharArray()[i];

                    if ((c == '§') && (i < word.toCharArray().length - 1)) {
                        char code = word.toCharArray()[i + 1];

                        if (code == 'r') {
                            colorCodes = new StringBuilder();
                            continue;
                        }

                        colorCodes.append("§").append(word.toCharArray()[(i + 1)]);
                    }
                }

//                word = StringUtils.removeFormattingCodes(word);

                if (word.contains("\n")) {

                    String[] split = word.split("\n");

                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];

                        if (getStringWidth(currentWord + s + " ") < width) {
                            currentWord.append(s).append(" ");
                        } else {
                            finalWords.add(currentWord.toString());
                            currentWord = new StringBuilder(colorCodes + s + " ");
                        }

//                        currentWord.append("\n");
                        if (i != split.length - 1) {
                            finalWords.add(colorCodes.toString() + currentWord + " ");
                            currentWord = new StringBuilder();
                        }
                    }

                } else {
                    if (getStringWidth(currentWord + word + " ") < width) {
                        currentWord.append(word).append(" ");
                    } else {
                        finalWords.add(currentWord.toString());
                        currentWord = new StringBuilder(colorCodes + word + " ");
                    }
                }


            }

            if (currentWord.length() > 0)
                if (getStringWidth(currentWord.toString()) < width) {
                    finalWords.add(colorCodes.toString() + currentWord + " ");
                    currentWord = new StringBuilder();
                } else {
                    finalWords.addAll(formatString(currentWord.toString(), width));
                }
        } else {
            finalWords.add(text);
        }

        return finalWords;
    }

    public List<String> formatString(String string, double width) {
        List<String> finalWords = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();
        StringBuilder colorCodes = new StringBuilder();
        char[] chars = string.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if ((c == '§') && (i < chars.length - 1)) {
                char code = chars[(i + 1)];

                if (code == 'r')
                    colorCodes = new StringBuilder();
                else
                    colorCodes.append("§").append(colorCodes);

            }

            if (getStringWidth(currentWord.toString() + c) < width) {
                currentWord.append(c);
            } else {
                finalWords.add(currentWord.toString());
                currentWord = new StringBuilder(colorCodes.toString() + c);
            }
        }

        if (currentWord.length() > 0) {
            finalWords.add(currentWord.toString());
        }

        return finalWords;
    }

    private void setupMinecraftColorcodes() {
        for (int index = 0; index < 32; index++) {
            int noClue = (index >> 3 & 0x1) * 85;
            int red = (index >> 2 & 0x1) * 170 + noClue;
            int green = (index >> 1 & 0x1) * 170 + noClue;
            int blue = (index >> 0 & 0x1) * 170 + noClue;

            if (index == 6) {
                red += 85;
            }

            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            this.colorCode[index] = ((red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF);
        }
    }

    public String[] fitWidth(String text, double width) {
        List<String> split = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        double w = 0;
        for (int i = 0; i < text.toCharArray().length; i++) {
            char c = text.toCharArray()[i];
            String s = Character.toString(c);

            if (c == '\247') {
                i += 1;
                continue;
            }

            double tWidth = this.getStringWidth(s);

            if (w + tWidth < width) {
                sb.append(s);
                w += tWidth;
            } else {
                if (s.equals(" ")) {
                    split.add(sb.toString());
                    sb = new StringBuilder(s);
                    w = this.getStringWidth(s);
                } else {
                    int lastSpace = sb.toString().lastIndexOf(" ");
                    if (lastSpace != -1) {
                        String res = sb.substring(0, lastSpace);
                        split.add(res);
                        i = text.indexOf(res) + res.length();
                        sb = new StringBuilder();
                    } else {
                        split.add(sb.toString());
                        sb = new StringBuilder(s);
                    }
                    w = 0;
                }
            }
        }
        if (sb.length() != 0) {
            split.add(sb.toString());
        }

        return split.toArray(new String[0]);
    }

    public boolean isTextWidthLargerThanWidth(String str, double width) {
        double w = 0;

        for (int i = 0; i < str.toCharArray().length; i++) {
            String s = Character.toString(str.toCharArray()[i]);

            double tWidth = this.getStringWidth(s);

            if (w + tWidth > width)
                return true;
            else
                w += tWidth;

        }

        return false;
    }

    public float drawCenteredStringWithBetterShadow(String text, double x, double y, int color) {
        TexturedShadow.drawFontShadow(x - getStringWidth(text) / 2.0, y, this.getStringWidth(text) + 1, this.getHeight() - 1);
        return drawString(text, x - getStringWidth(text) / 2.0, y, color);
    }
}