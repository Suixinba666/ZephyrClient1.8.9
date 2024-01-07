package tech.imxianyu.utils.lyric;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.management.WidgetsManager;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.BloomShader;
import tech.imxianyu.rendering.shader.GlowShader;
import tech.imxianyu.rendering.shader.ShaderBlur;
import tech.imxianyu.rendering.shader.TextBlur;
import tech.imxianyu.widget.impl.MusicLyrics;

import java.awt.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class LyricBean {
    @Getter
    @NonNull
    public long timeStamp;

    @Getter
    final String time;
    @Getter
    @NonNull
    public String lyric;
    @Getter
    public String cLyric, rLyric;

    public int alpha = 160;
    public double scale = 0.8;
    public float blur = 0.0f;
    public double scrollWidth = 0, scrollWidthEMLP = 0;
    public double offsetX = 0;
    float amp = 0;
    public int scrollIdx = 0;
    public double scrollOffset = 0;
    Framebuffer fontAll;
    Framebuffer bloomOut;

    public void renderLine(ZFontRenderer lyricRenderer, ZFontRenderer transLyricRenderer, double x, double y, int alpha, boolean hasRenderEffect) {
        fontAll = RenderSystem.createFrameBuffer(fontAll);
        float roundBlur = new BigDecimal(blur).round(new MathContext(5)).floatValue();
        roundBlur = roundBlur <= 0.1f ? 0 : roundBlur;
//        if (roundBlur > 0) {
            fontAll.framebufferClear();
            fontAll.bindFramebuffer(true);
//        }
//        GlStateManager.blendFunc(770, 771);
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//        if (!lyric.isEmpty())
        if (hasRenderEffect) {
            renderSlideUpLyrics(x, y, alpha, true);
        } else {
            lyricRenderer.drawString(lyric, x, y, RenderSystem.hexColor(255, 255, 255, alpha));
        }
        String secondaryLyric = "";
        if (MusicLyrics.hasSecondaryLyrics())
            secondaryLyric = MusicLyrics.getSecondaryLyrics(this);

        if (!secondaryLyric.isEmpty()) {
            transLyricRenderer.drawString(secondaryLyric, x, y + lyricRenderer.getStringHeight(lyric) + 4, RenderSystem.hexColor(255, 255, 255, 50));
        }
//        if (roundBlur > 0) {
            fontAll.unbindFramebuffer();

            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
            TextBlur.render(fontAll.framebufferTexture, roundBlur, 1, 1);
//        }

//        transLyricRenderer.drawString(roundBlur + "", x + 200, y, RenderSystem.hexColor(255, 255, 255, 255));
//        transLyricRenderer.drawString(alpha + "", x + 200, y, RenderSystem.hexColor(255, 255, 255, 255));
    }

    public void renderSlideUpLyrics(double x, double y, int alpha, boolean background) {
        bloomOut = RenderSystem.createFrameBuffer(bloomOut);
        ZFontRenderer font = FontManager.pf40Medium;
        double yh = y;
        int line = 0;
        for (String eachLine : lyric.split("\n")) {
            double widthHere = 0;
            double progressHere = scrollWidthEMLP - 250 * line - (10/*offset*/);
            int index = 0;
            int lastPartWord = eachLine.lastIndexOf(" ");
            if (lastPartWord == -1) {
                lastPartWord = 0;
            }
            for (char c : eachLine.toCharArray()) {
                if (c == ' ') {
                    widthHere += font.getStringWidth(String.valueOf(c)) + 1;
                    continue;
                }
                double slideUp = MathHelper.clamp_double((widthHere - progressHere) * 0.5, 0, 5);
                float returned = 0;
                returned = (float) (progressHere - font.getStringWidth(eachLine.substring(0, lastPartWord))) / font.getStringWidth(eachLine.substring(lastPartWord));
                returned = MathHelper.clamp_float(returned * 1f, 0, 1);
                if (index >= lastPartWord && background) {
                    bloomOut.framebufferClear();
                    bloomOut.bindFramebuffer(true);
                    font.drawString(String.valueOf(c), x + widthHere, yh + slideUp * 0.1, Color.WHITE.getRGB());
                    bloomOut.unbindFramebuffer();

                    if (returned > 0.02f)
                        amp = AnimationSystem.interpolate(amp, 1, 0.02f);
//                    System.out.println(amp);
                    GlowShader.renderGlow(bloomOut.framebufferTexture, (int) (amp * 10), amp * 0.6f, Color.WHITE.getRGB(), 2);
                } else if (returned < 0.02f) {
                    amp = AnimationSystem.interpolate(amp, 0, 0.75f);
                }
                font.drawString(String.valueOf(c), x + widthHere, yh + slideUp * 0.1, RenderSystem.hexColor(255, 255, 255, alpha));
                widthHere += font.getStringWidth(String.valueOf(c));
                index++;
            }
            yh += font.getHeight();
            line++;
        }
    }
}
