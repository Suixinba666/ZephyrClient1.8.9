package tech.imxianyu.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.ShaderRenderer;

import java.awt.*;

/**
 * @author ImXianyu
 * @since 4/24/2023 9:10 AM
 */
public class RoundedRect {

    private static final ShaderRenderer roundedTexturedShader = new ShaderRenderer("Zephyr/shaders/roundRectTextured.frag");
    private static final ShaderRenderer roundedGradientShader = new ShaderRenderer("Zephyr/shaders/roundedRectGradient.frag");
    public static ShaderRenderer roundedShader = new ShaderRenderer("Zephyr/shaders/roundedRect.frag");
    public static ShaderRenderer roundedOutlineShader = new ShaderRenderer("Zephyr/shaders/roundRectOutline.frag");

    public static void drawRound(double x, double y, double width, double height, double radius, Color color) {
        drawRound(x, y, width, height, radius, false, color);
    }

    public static void drawRound(double x, double y, double width, double height, double radius, int color) {
        int a = (color >> 24 & 255);
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);
        drawRound(x, y, width, height, radius, false, new Color(r, g, b, a));
    }

    public static void drawRound(double x, double y, double width, double height, double radius, double shrink, Color color) {
        drawRound(x + shrink, y + shrink, width - shrink * 2, height - shrink * 2, radius, false, color);
    }

    public static void drawRoundScale(double x, double y, double width, double height, double radius, Color color, double scale) {
        drawRound(x + width - width * scale, y + height / 2f - ((height / 2f) * scale),
                width * scale, height * scale, radius, false, color);
    }

    public static void drawGradientHorizontal(double x, double y, double width, double height, double radius, Color left, Color right) {
        drawGradientRound(x, y, width, height, radius, left, left, right, right);
    }

    public static void drawGradientVertical(double x, double y, double width, double height, double radius, Color top, Color bottom) {
        drawGradientRound(x, y, width, height, radius, bottom, top, bottom, top);
    }

    public static void drawGradientCornerLR(double x, double y, double width, double height, double radius, Color topLeft, Color bottomRight) {
        Color mixedColor = new Color(ColorUtils.interpolateColor(topLeft.getRGB(), bottomRight.getRGB(), 0.2f));
        drawGradientRound(x, y, width, height, radius, mixedColor, topLeft, bottomRight, mixedColor);
    }

    public static void drawGradientCornerRL(double x, double y, double width, double height, double radius, Color bottomLeft, Color topRight) {
        Color mixedColor = new Color(ColorUtils.interpolateColor(bottomLeft.getRGB(), topRight.getRGB(), 0.2f));
        drawGradientRound(x, y, width, height, radius, bottomLeft, mixedColor, mixedColor, topRight);
    }

    public static void drawGradientRound(double x, double y, double width, double height, double radius, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        RenderSystem.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        roundedGradientShader.init();
        setupRoundedRectUniforms((float) x, (float) y, (float) width, (float) height, (float) radius, roundedGradientShader);
        // Bottom Left
        roundedGradientShader.setUniformf("color1", bottomLeft.getRed() * 0.003921568627451f, bottomLeft.getGreen() * 0.003921568627451f, bottomLeft.getBlue() * 0.003921568627451f, bottomLeft.getAlpha() * 0.003921568627451f);
        //Top left
        roundedGradientShader.setUniformf("color2", topLeft.getRed() * 0.003921568627451f, topLeft.getGreen() * 0.003921568627451f, topLeft.getBlue() * 0.003921568627451f, topLeft.getAlpha() * 0.003921568627451f);
        //Bottom Right
        roundedGradientShader.setUniformf("color3", bottomRight.getRed() * 0.003921568627451f, bottomRight.getGreen() * 0.003921568627451f, bottomRight.getBlue() * 0.003921568627451f, bottomRight.getAlpha() * 0.003921568627451f);
        //Top Right
        roundedGradientShader.setUniformf("color4", topRight.getRed() * 0.003921568627451f, topRight.getGreen() * 0.003921568627451f, topRight.getBlue() * 0.003921568627451f, topRight.getAlpha() * 0.003921568627451f);
        ShaderRenderer.drawQuads((float) x - 1, (float) y - 1, (float) width + 2, (float) height + 2);
        roundedGradientShader.unload();
        GlStateManager.disableBlend();
    }


    public static void drawRound(double x, double y, double width, double height, double radius, boolean blur, Color color) {
        RenderSystem.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        roundedShader.init();

        setupRoundedRectUniforms((float) x, (float) y, (float) width, (float) height, (float) radius, roundedShader);
        roundedShader.setUniformi("blur", blur ? 1 : 0);
        roundedShader.setUniformf("color", color.getRed() * 0.003921568627451f, color.getGreen() * 0.003921568627451f, color.getBlue() * 0.003921568627451f, color.getAlpha() * 0.003921568627451f);

        ShaderRenderer.drawQuads((float) x - 1, (float) y - 1, (float) width + 2, (float) height + 2);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }


    public static void drawRoundOutline(double x, double y, double width, double height, double radius, double outlineThickness, Color color, Color outlineColor) {
        GlStateManager.pushMatrix();
        RenderSystem.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        roundedOutlineShader.init();

        setupRoundedRectUniforms((float) (x), (float) (y), (float) width, (float) height, (float) radius, roundedOutlineShader);
        roundedOutlineShader.setUniformf("outlineThickness", (float) (outlineThickness));
        roundedOutlineShader.setUniformf("color", color.getRed() * 0.003921568627451f, color.getGreen() * 0.003921568627451f, color.getBlue() * 0.003921568627451f, color.getAlpha() * 0.003921568627451f);
        roundedOutlineShader.setUniformf("outlineColor", outlineColor.getRed() * 0.003921568627451f, outlineColor.getGreen() * 0.003921568627451f, outlineColor.getBlue() * 0.003921568627451f, outlineColor.getAlpha() * 0.003921568627451f);


        ShaderRenderer.drawQuads((float) (x - (2 + outlineThickness)), (float) (y - (2 + outlineThickness)), (float) (width + (4 + outlineThickness * 2)) * 2, (float) (height + (4 + outlineThickness * 2)) * 2);
        roundedOutlineShader.unload();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    public static void drawRoundTextured(double x, double y, double width, double height, double radius, double alpha, int textureId) {
        RenderSystem.resetColor();
        roundedTexturedShader.init();
        roundedTexturedShader.setUniformi("textureIn", textureId);
        setupRoundedRectUniforms(x, y, width, height, radius, roundedTexturedShader);
        roundedTexturedShader.setUniformf("alpha", (float) alpha);
        ShaderRenderer.drawQuads((float) x - 1, (float) y - 1, (float) width + 2, (float) height + 2);
        roundedTexturedShader.unload();
        GlStateManager.disableBlend();
    }

    private static void setupRoundedRectUniforms(double x, double y, double width, double height, double radius, ShaderRenderer roundedTexturedShader) {
        roundedTexturedShader.setUniformf("location", (float) (x),
                (Minecraft.getMinecraft().displayHeight - (float) (height)) - (float) (y));
        roundedTexturedShader.setUniformf("rectSize", (float) (width), (float) (height));
        roundedTexturedShader.setUniformf("radius", (float) (radius));
    }

}
