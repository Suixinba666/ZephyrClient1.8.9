package tech.imxianyu.rendering.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL11;
public class GlowShader {
    private static final Minecraft mc = Minecraft.getMinecraft();
    static FloatBuffer weights = BufferUtils.createFloatBuffer(256);
    private static ShaderRenderer offsetShader;
    private static Framebuffer framebuffer;
    private static float lastRadius;

    private static float calculateGaussianValue(float x, float sigma) {
        double PI = 3.141592653;
        double output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    public static void renderGlow(int sourceTexture, int radius, float alpha, int glowColor, int iteration) {
        if (offsetShader == null) {
            offsetShader = new ShaderRenderer("Zephyr/shaders/glow.fsh");
        }
        framebuffer = RenderSystem.createFrameBuffer(framebuffer);

        float r = RenderSystem.getRedFormInt(glowColor);
        float g = RenderSystem.getGreenFormInt(glowColor);
        float b = RenderSystem.getBlueFormInt(glowColor);

//        GL11.glEnable(3001);
        GlStateManager.alphaFunc(519, 0.0f);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

        if (!weights.hasArray() || radius != lastRadius) {
            for (int i = 0; i < radius; i++) {
                weights.put(calculateGaussianValue(i, radius / 2));
            }
        }
        weights.rewind();

        lastRadius = radius;

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
//        System.out.println(String.format("%f %f %f", r, g, b));

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        offsetShader.init();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(sourceTexture);

        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weights);
        GL20.glUniform1f(offsetShader.getUniform("alpha"), alpha);
        GL20.glUniform3f(offsetShader.getUniform("bloomColor"), r, g, b);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 0, 1);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);
        ShaderRenderer.drawQuads();
        framebuffer.unbindFramebuffer();
        offsetShader.unload();

        mc.getFramebuffer().bindFramebuffer(true);
        offsetShader.init();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(framebuffer.framebufferTexture);

        GL20.glUniform1i(offsetShader.getUniform("inTexture"), 0);
        GL20.glUniform1i(offsetShader.getUniform("radius"), radius);
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weights);
        GL20.glUniform1f(offsetShader.getUniform("alpha"), alpha);
        GL20.glUniform3f(offsetShader.getUniform("bloomColor"), r, g, b);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 1, 0);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);
        for (int i = 0; i <= iteration; i++) {
            ShaderRenderer.drawQuads();
        }
        offsetShader.unload();

        RenderSystem.resetColor();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);
    }

    public static void destory() {
        /*offsetShader.cleanup();
        offsetShader = null;*/
    }
}
