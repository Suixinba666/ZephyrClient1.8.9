package tech.imxianyu.rendering;

import net.minecraft.client.Minecraft;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.awt.*;

public class ShaderUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void doRectBlur(double x, double y, double width, double height) {
        doRectBlur(x, y, width, height, 255);
    }

    public static void doRectBlur(double x, double y, double width, double height, float alpha) {
        doRectBlur(x, y, width, height, (int) (alpha * 255));
    }

    public static void doRectBlur(double x, double y, double width, double height, int alpha) {
        Blur.blurBuffer.bindFramebuffer(true);
        Rect.draw(x, y, width, height, RenderSystem.hexColor(255, 255, 255, alpha), Rect.RectType.EXPAND);

        mc.getFramebuffer().bindFramebuffer(true);
    }

    public static void doRoundedBlur(double x, double y, double width, double height, double radius) {
        doRoundedBlur(x, y, width, height, radius, 255);
    }

    public static void doRoundedBlur(double x, double y, double width, double height, double radius, float alpha) {
        doRoundedBlur(x, y, width, height, radius, (int) (alpha * 255));
    }

    public static void doRoundedBlur(double x, double y, double width, double height, double radius, int alpha) {
        Blur.blurBuffer.bindFramebuffer(true);
        RoundedRect.drawRound(x, y, width, height, radius, new Color(255, 255, 255, alpha));

        mc.getFramebuffer().bindFramebuffer(true);
    }

    public static void doRectBloom(double x, double y, double width, double height) {
        doRectBloom(x, y, width, height, 255);
    }

    public static void doRectBloom(double x, double y, double width, double height, float alpha) {
        doRectBloom(x, y, width, height, (int) (alpha * 255));
    }

    public static void doRectBloom(double x, double y, double width, double height, int alpha) {
        Bloom.bloomBuffer.bindFramebuffer(true);
        Rect.draw(x, y, width, height, RenderSystem.hexColor(255, 255, 255, alpha), Rect.RectType.EXPAND);

        mc.getFramebuffer().bindFramebuffer(true);
    }

    public static void doRoundedBloom(double x, double y, double width, double height, double radius) {
        doRoundedBloom(x, y, width, height, radius, 255);
    }

    public static void doRoundedBloom(double x, double y, double width, double height, double radius, float alpha) {
        doRoundedBloom(x, y, width, height, radius, (int) (alpha * 255));
    }

    public static void doRoundedBloom(double x, double y, double width, double height, double radius, int alpha) {
        Bloom.bloomBuffer.bindFramebuffer(true);
        RoundedRect.drawRound(x, y, width, height, radius, new Color(255, 255, 255, alpha));

        mc.getFramebuffer().bindFramebuffer(true);
    }

    //..?
    public static void doRectBlurAndBloom(double x, double y, double width, double height) {
        doRectBlurAndBloom(x, y, width, height, 255);
    }

    public static void doRectBlurAndBloom(double x, double y, double width, double height, float alpha) {
        doRectBlurAndBloom(x, y, width, height, (int) (alpha * 255));
    }

    public static void doRectBlurAndBloom(double x, double y, double width, double height, int alpha) {
        Blur.blurBuffer.bindFramebuffer(true);
        Rect.draw(x, y, width, height, RenderSystem.hexColor(255, 255, 255, alpha), Rect.RectType.EXPAND);

        Bloom.bloomBuffer.bindFramebuffer(true);
        Rect.draw(x, y, width, height, RenderSystem.hexColor(255, 255, 255, alpha), Rect.RectType.EXPAND);

        mc.getFramebuffer().bindFramebuffer(true);
    }

    public static void doRoundedBlurAndBloom(double x, double y, double width, double height, double radius) {
        doRoundedBlurAndBloom(x, y, width, height, radius, 255);
    }

    public static void doRoundedBlurAndBloom(double x, double y, double width, double height, double radius, float alpha) {
        doRoundedBlurAndBloom(x, y, width, height, radius, (int) (alpha * 255));
    }

    public static void doRoundedBlurAndBloom(double x, double y, double width, double height, double radius, int alpha) {
        Blur.blurBuffer.bindFramebuffer(true);
        RoundedRect.drawRound(x, y, width, height, radius, new Color(255, 255, 255, alpha));

        Bloom.bloomBuffer.bindFramebuffer(true);
        RoundedRect.drawRound(x, y, width, height, radius, new Color(255, 255, 255, alpha));

        mc.getFramebuffer().bindFramebuffer(true);
    }

}
