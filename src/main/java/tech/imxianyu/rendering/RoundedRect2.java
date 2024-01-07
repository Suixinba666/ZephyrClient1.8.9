package tech.imxianyu.rendering;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.ShaderRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RoundedRect2 {
    double lastwidth;
    double lastheight;
    double lastradius;
    public DynamicTexture tex;

    public RoundedRect2() {
        super();
    }

    public void draw(double x, double y, double width, double height, double radius, int color)  {
        if (tex == null || lastwidth != width || lastheight != height || lastradius != radius) {
            BufferedImage bufferedImage = new BufferedImage((int) width * 2, (int) height * 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
            g.setColor(Color.WHITE);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.fillRoundRect(0, 0, (int) (width * 2), (int) (height * 2), (int) radius, (int) radius);

            tex = new DynamicTexture(bufferedImage);
        }

        lastwidth = width;
        lastheight = height;
        lastradius = radius;
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.bindTexture(tex.getGlTextureId());
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        RenderSystem.color(color);
        ShaderRenderer.drawBind(x * 2, y * 2, width * 2, height * 2);
        GL11.glScalef(2.0f, 2.0f, 2.0f);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }
}
