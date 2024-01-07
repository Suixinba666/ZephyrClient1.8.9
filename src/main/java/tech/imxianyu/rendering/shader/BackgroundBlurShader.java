package tech.imxianyu.rendering.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.nio.FloatBuffer;

public class BackgroundBlurShader {
    private static final Minecraft mc = Minecraft.getMinecraft();
    static float[] weightsGaussian = new float[256];
    static FloatBuffer weights = BufferUtils.createFloatBuffer(256);
    private static ShaderRenderer offsetShader;
    private static ShaderRenderer offsetShader2;
    private static Framebuffer framebuffer;
    private static Framebuffer framebuffer2;
    private static float lastRadius;

    static float SCurve(float x) {


        // ---- by CeeJayDK

        x = (float) (x * 2.0 - 1.0);
        return (float) (-x * Math.abs(x) * 0.5 + x + 0.5);

        //return dot(vec3(-x, 2.0, 1.0 ),vec3(abs(x), x, 1.0)) * 0.5; // possibly faster version


        // ---- original for posterity

        // How to do this without if-then-else?
        // +edited the too steep curve value

        // if (value < 0.5)
        // {
        //    return value * value * 2.0;
        // }

        // else
        // {
        // 	value -= 1.0;

        // 	return 1.0 - value * value * 2.0;
        // }
    }

    private static float calculateGaussianValue(float x, float sigma) {
        double PI = 3.141592653;
        double output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    public static void render(int stencilSource, int radius, float factor) {
        if (offsetShader == null) {
            offsetShader = new ShaderRenderer("Zephyr/shaders/backgroundBlur.fsh");
        }
        if (offsetShader2 == null) {
            offsetShader2 = new ShaderRenderer("Zephyr/shaders/stencil.fsh");
        }
        framebuffer = RenderSystem.createFrameBuffer(framebuffer);
        framebuffer2 = RenderSystem.createFrameBuffer(framebuffer2);
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
//        if (radius == 0) return;

        if (!weights.hasArray() || radius != lastRadius) {
            for (int i = 0; i < radius; i++) {
                weights.put(calculateGaussianValue(i, radius / 2));
            }
//            if (radius == 1) weights.put(1);
        }
        weights.rewind();

        lastRadius = radius;

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

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
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weights);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 0, 1);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);

        RenderSystem.bindTexture(mc.getFramebuffer().framebufferTexture);

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
        GL20.glUniform1fv(offsetShader.getUniform("offset"), weights);
        GL20.glUniform2f(offsetShader.getUniform("offsetDir"), 1, 0);
        GL20.glUniform2f(offsetShader.getUniform("texelSize"), 1f / framebuffer.framebufferWidth, 1f / framebuffer.framebufferHeight);

        RenderSystem.bindTexture(framebuffer.framebufferTexture);

        ShaderRenderer.drawQuads();
        framebuffer2.unbindFramebuffer();
        offsetShader.unload();

        mc.getFramebuffer().bindFramebuffer(true);
//        offsetShader2.init();
//        GL20.glUniform1i(offsetShader2.getUniform("mixTexture"), 16);
//        GL20.glUniform1i(offsetShader2.getUniform("stencilTexture"), 0);
//
//        GL13.glActiveTexture(GL13.GL_TEXTURE0);
//        RenderSystem.bindTexture(stencilSource);
//        GL13.glActiveTexture(GL13.GL_TEXTURE16);
//        RenderSystem.bindTexture(framebuffer2.framebufferTexture);
//        offsetShader2.drawQuads();
//        offsetShader2.unload();

        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);

//        RenderUtil.resetColor();
    }

    public static void destory() {/*
        if (offsetShader != null)
            offsetShader.cleanup();
        offsetShader = null;
        if (offsetShader2 != null)
            offsetShader2.cleanup();
        offsetShader2 = null;*/
    }
}
