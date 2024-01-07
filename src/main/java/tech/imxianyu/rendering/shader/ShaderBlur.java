package tech.imxianyu.rendering.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjglx.opengl.Display;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.utils.timing.Timer;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL11;

/**
 * @author ImXianyu
 * @since 2/18/2023 9:29 PM
 */
public class ShaderBlur {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final ShaderRenderer offsetShader = new ShaderRenderer("Zephyr/shaders/backgroundBlur.fsh");
    private static final ShaderRenderer offsetShader3 = new ShaderRenderer("Zephyr/shaders/blur.fsh");
    private static final ShaderRenderer offsetShader2 = new ShaderRenderer("Zephyr/shaders/stencil.fsh");
    private static Framebuffer framebuffer = new Framebuffer(1, 1, true);
    private static Framebuffer framebuffer2 = new Framebuffer(1, 1, true);
    private static Framebuffer framebufferOut = new Framebuffer(1, 1, true);
    private static Framebuffer framebuffer3 = new Framebuffer(200, 20, false);
    private static float lastRadius;
    private static Framebuffer positionBlurBuffer;
    public static final Map<Integer, FloatBuffer> weightMap = new HashMap<>();

    private static Timer vsync = new Timer();

    private static float calculateGaussianValue(float x, float sigma) {
        double PI = 3.141592653;
        double output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    static {


        for (int i = 0; i < 256; i++) {
            FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
            for (int j = 0; j <= i; j++) {
                weightBuffer.put(calculateGaussianValue(j, i));
            }
            weightBuffer.rewind();

            weightMap.put(i, weightBuffer);
        }

    }

    public static void render(int stencilSource, float factor) {
        if (vsync.isDelayed(Display.getDisplayMode().getFrequency() * 1000L, true)) {
            int radius = ZephyrSettings.shaderQuality.getValue().getRadius();
            double scaleFactor = ZephyrSettings.shaderQuality.getValue().getFactor();
//            framebuffer = RenderSystem.createFrameBuffer(framebuffer);
            framebuffer = RenderSystem.createDownScaledFrameBuffer(framebuffer, scaleFactor);
//            framebuffer2 = RenderSystem.createFrameBuffer(framebuffer2);
            framebuffer2 = RenderSystem.createDownScaledFrameBuffer(framebuffer2, scaleFactor);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
            GlStateManager.disableBlend();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

            FloatBuffer weightBuffer = weightMap.get(radius);

            RenderSystem.setAlphaLimit(0.0F);

            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);

            offsetShader.init();
            GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
            GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
            GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
            GL20.glUniform1f(offsetShader.getUniform("brightness"), 1);
            GL20.glUniform1f(offsetShader.getUniform("saturation"), 1);
            GL20.glUniform1f(offsetShader.getUniform("contrast"), 1);
            GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
            GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 0, 1);
            GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);

            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
            GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);

            ShaderRenderer.drawQuads();
            framebuffer.unbindFramebuffer();
            offsetShader.unload();

            mc.getFramebuffer().bindFramebuffer(true);
            framebuffer2.framebufferClear();
            framebuffer2.bindFramebuffer(true);
            offsetShader.init();
            GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
            GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
            GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
            GL20.glUniform1f(offsetShader.getUniform("brightness"), 1);
            GL20.glUniform1f(offsetShader.getUniform("saturation"), 1);
            GL20.glUniform1f(offsetShader.getUniform("contrast"), 1);
            GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
            GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 1, 0);
            GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);

            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
            GlStateManager.bindTexture(framebuffer.framebufferTexture);

            ShaderRenderer.drawQuads();
            framebuffer2.unbindFramebuffer();
            offsetShader.unload();

            mc.getFramebuffer().bindFramebuffer(true);
            offsetShader2.init();
            GL20.glUniform1i(offsetShader2.getUniform("mixTexture"), 16);
            GL20.glUniform1i(offsetShader2.getUniform("stencilTexture"), 0);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GlStateManager.bindTexture(stencilSource);
            GL13.glActiveTexture(GL13.GL_TEXTURE16);
            GlStateManager.bindTexture(framebuffer2.framebufferTexture);
            ShaderRenderer.drawQuads();
            offsetShader2.unload();

            GL13.glActiveTexture(GL13.GL_TEXTURE16);
            GlStateManager.bindTexture(0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GlStateManager.bindTexture(0);
        }
    }

    public static void render(int stencilSource, int radius, float factor) {
        framebuffer = RenderSystem.createFrameBuffer(framebuffer);
        framebuffer2 = RenderSystem.createFrameBuffer(framebuffer2);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        if (radius == 0) return;

        FloatBuffer weightBuffer = weightMap.get(radius);

        RenderSystem.setAlphaLimit(0.0F);

//        framebuffer2.framebufferClear(mc.IS_RUNNING_ON_MAC);
//        framebuffer2.bindFramebuffer(true);
//
//        offsetShader2.bind();
//        GL20.glUniform1i(offsetShader2.getUniform("inTexture"), 0);
//        GL20.glUniform1f(offsetShader2.getUniform("factor"), factor);
//
//        RenderSystem.bindTexture(mc.getFramebuffer().framebufferTexture);
//        offsetShader2.drawQuads();
//        framebuffer2.unbindFramebuffer();
//        offsetShader2.unbind();

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("stencilTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), 1);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), 1);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), 1);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 0, 1);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);

        ShaderRenderer.drawQuads();
        framebuffer.unbindFramebuffer();
        offsetShader.unload();

        mc.getFramebuffer().bindFramebuffer(true);
        framebuffer2.framebufferClear();
        framebuffer2.bindFramebuffer(true);
        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), 1);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), 1);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), 1);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 1, 0);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(framebuffer.framebufferTexture);

        ShaderRenderer.drawQuads();
        framebuffer2.unbindFramebuffer();
        offsetShader.unload();

        mc.getFramebuffer().bindFramebuffer(true);
        offsetShader2.init();
        GL20.glUniform1i(offsetShader2.getUniform("mixTexture"), 16);
        GL20.glUniform1i(offsetShader2.getUniform("stencilTexture"), 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(stencilSource);
        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(framebuffer2.framebufferTexture);
        ShaderRenderer.drawQuads();
        offsetShader2.unload();

        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);

//        RenderUtil.resetColor();
    }

    public static void render(int stencilSource, int radius, float factor, double scale) {
        framebuffer = RenderSystem.createFrameBuffer(framebuffer);
        framebuffer2 = RenderSystem.createFrameBuffer(framebuffer2);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        if (radius == 0) return;

        FloatBuffer weightBuffer = weightMap.get(radius);

        RenderSystem.setAlphaLimit(0.0F);

//        framebuffer2.framebufferClear(mc.IS_RUNNING_ON_MAC);
//        framebuffer2.bindFramebuffer(true);
//
//        offsetShader2.bind();
//        GL20.glUniform1i(offsetShader2.getUniform("inTexture"), 0);
//        GL20.glUniform1f(offsetShader2.getUniform("factor"), factor);
//
//        RenderSystem.bindTexture(mc.getFramebuffer().framebufferTexture);
//        offsetShader2.drawQuads();
//        framebuffer2.unbindFramebuffer();
//        offsetShader2.unbind();

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("stencilTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), 1);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), 1);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), 1);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 0, 1);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);

        ShaderRenderer.drawQuads();
        framebuffer.unbindFramebuffer();
        offsetShader.unload();

        mc.getFramebuffer().bindFramebuffer(true);
        framebuffer2.framebufferClear();
        framebuffer2.bindFramebuffer(true);
        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), 1);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), 1);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), 1);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 1, 0);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(framebuffer.framebufferTexture);

        ShaderRenderer.drawQuads();
        framebuffer2.unbindFramebuffer();
        offsetShader.unload();

        mc.getFramebuffer().bindFramebuffer(true);
        offsetShader2.init();
        GL20.glUniform1i(offsetShader2.getUniform("mixTexture"), 16);
        GL20.glUniform1i(offsetShader2.getUniform("stencilTexture"), 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(stencilSource);
        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(framebuffer2.framebufferTexture);
        ShaderRenderer.drawQuads();
        offsetShader2.unload();

        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);

//        RenderUtil.resetColor();
    }

    public static Framebuffer renderSource(Framebuffer buffer, float radius, float factor, float brightness, float saturation, float contrast, double x, double y, double width, double height) {
        if (framebuffer3 == null || framebuffer3.framebufferWidth != buffer.framebufferWidth || framebuffer3.framebufferHeight != buffer.framebufferHeight) {
            framebuffer3 = new Framebuffer(buffer.framebufferWidth, buffer.framebufferHeight, false);
        }

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        if (radius == 0) return null;

        FloatBuffer weightBuffer = weightMap.get((int) radius);

        RenderSystem.setAlphaLimit(0.0F);

//        framebuffer2.framebufferClear(mc.IS_RUNNING_ON_MAC);
//        framebuffer2.bindFramebuffer(true);
//
//        offsetShader2.bind();
//        GL20.glUniform1i(offsetShader2.getUniform("inTexture"), 0);
//        GL20.glUniform1f(offsetShader2.getUniform("factor"), factor);
//
//        RenderSystem.bindTexture(mc.getFramebuffer().framebufferTexture);
//        offsetShader2.drawQuads();
//        framebuffer2.unbindFramebuffer();
//        offsetShader2.unbind();

        framebuffer3.framebufferClear();
        framebuffer3.bindFramebuffer(true);

        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1f(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), brightness);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), saturation);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), contrast);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 0, 1);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer3.framebufferWidth, 1f / framebuffer3.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(buffer.framebufferTexture);

        ShaderRenderer.drawQuads();
        framebuffer3.unbindFramebuffer();
        offsetShader.unload();

        mc.getFramebuffer().bindFramebuffer(true);

        framebuffer2.framebufferClear();
        framebuffer2.bindFramebuffer(true);
        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1f(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), brightness);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), saturation);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), contrast);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 1, 0);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer3.framebufferWidth, 1f / framebuffer3.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(framebuffer3.framebufferTexture);

        ShaderRenderer.drawBind(x, y, framebuffer3.framebufferWidth, framebuffer3.framebufferHeight);
        framebuffer2.unbindFramebuffer();
//        ShaderRenderer.drawQuads();
        offsetShader.unload();

        return framebuffer2;
    }

    public static Framebuffer renderSource2(Framebuffer buffer, int radius, float factor, float brightness, float saturation, float contrast) {
        if (framebuffer3 == null || framebuffer3.framebufferWidth != buffer.framebufferWidth || framebuffer3.framebufferHeight != buffer.framebufferHeight) {
            framebuffer3 = new Framebuffer(buffer.framebufferWidth, buffer.framebufferHeight, false);
        }
        if (framebufferOut == null || framebufferOut.framebufferWidth != buffer.framebufferWidth || framebufferOut.framebufferHeight != buffer.framebufferHeight) {
            framebufferOut = new Framebuffer(buffer.framebufferWidth, buffer.framebufferHeight, false);
        }

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        if (radius == 0) return null;

        FloatBuffer weightBuffer = weightMap.get(radius);

        RenderSystem.setAlphaLimit(0.0F);

//        framebuffer2.framebufferClear(mc.IS_RUNNING_ON_MAC);
//        framebuffer2.bindFramebuffer(true);
//
//        offsetShader2.bind();
//        GL20.glUniform1i(offsetShader2.getUniform("inTexture"), 0);
//        GL20.glUniform1f(offsetShader2.getUniform("factor"), factor);
//
//        RenderSystem.bindTexture(mc.getFramebuffer().framebufferTexture);
//        offsetShader2.drawQuads();
//        framebuffer2.unbindFramebuffer();
//        offsetShader2.unbind();

        framebuffer3.framebufferClear();
        framebuffer3.bindFramebuffer(true);

        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("stencilTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), brightness);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), saturation);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), contrast);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 0, 1);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer3.framebufferWidth, 1f / framebuffer3.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(buffer.framebufferTexture);

        ShaderRenderer.drawQuads();
        framebuffer3.unbindFramebuffer();
        offsetShader.unload();

        mc.getFramebuffer().bindFramebuffer(true);

        framebufferOut.framebufferClear();
        framebufferOut.bindFramebuffer(true);
        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), brightness);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), saturation);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), contrast);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 1, 0);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer3.framebufferWidth, 1f / framebuffer3.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(framebuffer3.framebufferTexture);

        ShaderRenderer.drawQuads();
        framebufferOut.unbindFramebuffer();
//        ShaderRenderer.drawQuads();
        offsetShader.unload();

        return framebufferOut;
    }

    public static void renderSource(int source, int radius, float factor, float brightness, float saturation, float contrast) {
        framebuffer3 = RenderSystem.createFrameBuffer(framebuffer3);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        if (radius == 0) return;

        FloatBuffer weightBuffer = weightMap.get(radius);

        RenderSystem.setAlphaLimit(0.0F);

//        framebuffer2.framebufferClear(mc.IS_RUNNING_ON_MAC);
//        framebuffer2.bindFramebuffer(true);
//
//        offsetShader2.bind();
//        GL20.glUniform1i(offsetShader2.getUniform("inTexture"), 0);
//        GL20.glUniform1f(offsetShader2.getUniform("factor"), factor);
//
//        RenderSystem.bindTexture(mc.getFramebuffer().framebufferTexture);
//        offsetShader2.drawQuads();
//        framebuffer2.unbindFramebuffer();
//        offsetShader2.unbind();

        framebuffer3.framebufferClear();
        framebuffer3.bindFramebuffer(true);

        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), brightness);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), saturation);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), contrast);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 0, 1);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer3.framebufferWidth, 1f / framebuffer3.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(source);

        ShaderRenderer.drawQuads();
        framebuffer3.unbindFramebuffer();
        offsetShader.unload();

        mc.getFramebuffer().bindFramebuffer(true);

        framebuffer2.framebufferClear();
        framebuffer2.bindFramebuffer(true);
        offsetShader.init();
        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1f(offsetShader.getUniform("factor"), factor);
        GL20.glUniform1f(offsetShader.getUniform("brightness"), brightness);
        GL20.glUniform1f(offsetShader.getUniform("saturation"), saturation);
        GL20.glUniform1f(offsetShader.getUniform("contrast"), contrast);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weightBuffer);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 1, 0);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer3.framebufferWidth, 1f / framebuffer3.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(framebuffer3.framebufferTexture);

        ShaderRenderer.drawQuads();
        framebuffer2.unbindFramebuffer();
        offsetShader.unload();
    }
}
