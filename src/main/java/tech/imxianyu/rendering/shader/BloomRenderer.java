package tech.imxianyu.rendering.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ImXianyu
 * @since 4/30/2023 6:15 PM
 */
public class BloomRenderer {


    public static final Map<Integer, FloatBuffer> weightMap = new HashMap<>();
    public static final boolean bloomEnabled = true;
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static ShaderRenderer gaussianBloom = new ShaderRenderer("Zephyr/shaders/bloom.frag");
    public static Framebuffer framebuffer = new Framebuffer(1, 1, false);
    private static Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);

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

    public static void preRenderShadow() {
        if (!bloomEnabled)
            return;

        bloomFramebuffer = RenderSystem.createFrameBuffer(bloomFramebuffer);

        bloomFramebuffer.framebufferClear();
        bloomFramebuffer.bindFramebuffer(true);
    }

    public static void postRenderShadow() {
        if (!bloomEnabled)
            return;

        BloomRenderer.renderBlur(bloomFramebuffer.framebufferTexture, 10, 2);
    }

    public static void postRenderShadow(float alpha) {
        if (!bloomEnabled)
            return;

        BloomRenderer.renderBlur(bloomFramebuffer.framebufferTexture, 10, 2, alpha);
    }

    public static void renderBlur(int sourceTexture, int radius, int offset) {
        renderBlur(sourceTexture, radius, offset, 1);
    }

    public static void renderBlur(int sourceTexture, int radius, int offset, float alpha) {
        framebuffer = RenderSystem.createFrameBuffer(framebuffer);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        FloatBuffer weightBuffer = weightMap.get(radius);

        RenderSystem.setAlphaLimit(0.0F);

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        gaussianBloom.init();
        setupUniforms(radius, offset, 0, weightBuffer);
        RenderSystem.bindTexture(sourceTexture);
        ShaderRenderer.drawQuads(alpha);
        gaussianBloom.unload();
        framebuffer.unbindFramebuffer();


        mc.getFramebuffer().bindFramebuffer(true);

        gaussianBloom.init();
        setupUniforms(radius, 0, offset, weightBuffer);
        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        RenderSystem.bindTexture(sourceTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(framebuffer.framebufferTexture);
        ShaderRenderer.drawQuads(alpha);
        gaussianBloom.unload();

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.enableAlpha();

        GlStateManager.bindTexture(0);
    }

    public static void setupUniforms(int radius, int directionX, int directionY, FloatBuffer weights) {
        gaussianBloom.setUniformi("inTexture", 0);
        gaussianBloom.setUniformi("textureToCheck", 16);
        gaussianBloom.setUniformf("radius", radius);
        gaussianBloom.setUniformf("texelSize", 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        gaussianBloom.setUniformf("direction", directionX, directionY);
        GL20.glUniform1fv(gaussianBloom.getUniform("weights"), weights);
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double PI = 3.14159265358979323846;
        double output = 1 / Math.sqrt(2.0 * PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }


    /**
     * @return 1 / sqrt(x)
     */
    public static double fastInverseSqrt(double x) {
        double halfX = 0.5d * x;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        x = Double.longBitsToDouble(i);
        x *= (1.5d - halfX * x * x);
        return x;
    }

}
