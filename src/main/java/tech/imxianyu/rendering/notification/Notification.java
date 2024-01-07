package tech.imxianyu.rendering.notification;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.Blur;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.utils.timing.Timer;

import java.awt.*;
import java.text.DecimalFormat;

public class Notification {
    public static Minecraft mc = Minecraft.getMinecraft();

    public String message, title;
    public String icon;
    public Type type;
    public long stayTime;
    public double renderX = 1231234, renderY = 0, stencilWidth, height = 40;
    Timer timer;

    public Notification(String title, String message, Type type, long stayTime) {
        this.message = message;
        this.title = title;

        switch (type) {
            case INFO:
                this.icon = "A";
                break;
            case WARNING:
                this.icon = "B";
                break;
            case ERROR:
                this.icon = "C";
                break;
            case SUCCESS:
                this.icon = "D";
                break;
        }
        this.type = type;
        this.stayTime = stayTime;

        this.timer = new Timer();
        timer.reset();
    }

    DecimalFormat df = new DecimalFormat("##.#");

    public void draw(double offsetX, double offsetY) {

        if (renderY == 0)
            renderY = offsetY;
        else
            renderY = AnimationSystem.interpolate(renderY, offsetY, 0.2);

        double width = 200;


        if (renderX == 1231234)
            renderX = -width;
        else
            this.renderX = AnimationSystem.interpolate(this.renderX, timer.delayed().toMillis() > stayTime /*- 500*/ ? offsetX - width - 20: offsetX, 0.2f);

        if (!ZephyrSettings.reduceShaders.getValue()) {
            ShaderUtils.doRoundedBlurAndBloom(renderX, renderY, width, this.getHeight(), 3);
        } else {
            RoundedRect.drawRound(renderX, renderY, width, this.getHeight(), 3, new Color(ColorUtils.getColor(ColorUtils.ColorType.Base)));
        }

        FontManager.icon30.drawString(this.icon, renderX + 2, renderY + 4, ColorUtils.getColor(ColorUtils.ColorType.Text));

        ZFontRenderer fontRenderer = FontManager.pf18;

        String[] strings = fontRenderer.fitWidth(this.message, 180);
        height = 10 + (fontRenderer.getHeight() + 2) * (strings.length + 1);

        int fontColor = 0xff000000;
        switch (this.type) {
            case INFO:
                fontColor = 0xff4286f5;
                break;

            case WARNING:
                fontColor = 0xffefbc12;
                break;

            case ERROR:
                fontColor = 0xfff04747;
                break;

            case SUCCESS:
                fontColor = 0xff23ad5c;
        }

        fontRenderer.drawString(this.title, renderX + 6 + FontManager.icon30.getStringWidth(this.icon), renderY + 4.5, fontColor);

        double leftTime = (this.stayTime - timer.delayed().toMillis()) / 1000.0;

        String fmt = df.format(leftTime);

        if (fmt.length() == 1)
            fmt += ".0";

        fontRenderer.drawString(" (" + fmt + "s)", renderX + 8 + FontManager.icon30.getStringWidth(this.icon) + fontRenderer.getStringWidth(this.title), renderY + 4.5, -1);
        fontRenderer.drawString(String.join("\n", strings), renderX + 3, renderY + 10 + fontRenderer.getHeight(), -1);

//        FontManager.shs18.drawString(this.message, renderX + 6 + FontManager.icon30.getStringWidth(this.icon), renderY + 4.5, fontColor);

//        Stencil.dispose();
    }

    public boolean shouldDelete() {
        return isFinished() && (this.renderX <= -200);
    }

    public double getHeight() {
        return height;
    }

    private boolean isFinished() {
        return timer.isDelayed(stayTime);
    }

    public void drawArrow(float left, float top, float right, float bottom, int color) {
        float shiet;
        if (left < right) {
            shiet = left;
            left = right;
            right = shiet;
        }
        if (top < bottom) {
            shiet = top;
            top = bottom;
            bottom = shiet;
        }
        float a = (float) (color >> 24 & 255) * 0.003921568627451F;
        float b = (float) (color >> 16 & 255) * 0.003921568627451F;
        float c = (float) (color >> 8 & 255) * 0.003921568627451F;
        float d = (float) (color & 255) * 0.003921568627451F;
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 1);
        GlStateManager.color(b, c, d, a);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom + 6f, 0.0D).endVertex();
        worldRenderer.pos(right, bottom, 0.0D).endVertex();
        worldRenderer.pos(right, top, 0.0D).endVertex();
        worldRenderer.pos(left, top - 6f, 0.0D).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 2.0F);
    }

    public enum Type {
        INFO, WARNING, ERROR, SUCCESS
    }

}
