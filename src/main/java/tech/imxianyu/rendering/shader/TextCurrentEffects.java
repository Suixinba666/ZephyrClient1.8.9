package tech.imxianyu.rendering.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

/**
 * @author ImXianyu
 * @since 4/30/2023 6:15 PM
 */
public class TextCurrentEffects {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static ShaderRenderer shaderRenderer = new ShaderRenderer("Zephyr/shaders/blur.fsh");
    public static Framebuffer framebuffer = new Framebuffer(1, 1, false);

    public static void render(int sourceTexture, float radius) {
        framebuffer = RenderSystem.createFrameBuffer(framebuffer);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1, 1, 1, 1);

        RenderSystem.setAlphaLimit(0.0F);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        shaderRenderer.init();
        setupUniforms(radius, 1, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(sourceTexture);
        ShaderRenderer.drawQuads();
        shaderRenderer.unload();
        framebuffer.unbindFramebuffer();

        mc.getFramebuffer().bindFramebuffer(true);

        shaderRenderer.init();
        setupUniforms(radius, 0, 1);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(framebuffer.framebufferTexture);
        ShaderRenderer.drawQuads();
        shaderRenderer.unload();

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        GlStateManager.bindTexture(0);
    }

    public static void setupUniforms(float radius, float offsetDirX, float offsetDirY) {
        shaderRenderer.setUniformi("inTexture", 0);
        shaderRenderer.setUniformf("radius", radius);
        shaderRenderer.setUniformf("texelSize", 1.0F / (float) framebuffer.framebufferWidth, 1.0F / (float) framebuffer.framebufferHeight);
        shaderRenderer.setUniformf("offsetDir", offsetDirX, offsetDirY);
    }
}
