package tech.imxianyu.widget.impl;

import javafx.scene.media.MediaPlayer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.management.WidgetsManager;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.rendering.HSBColor;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.ColorSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.audio.AudioPlayer;
import tech.imxianyu.utils.math.MathUtils;
import tech.imxianyu.widget.Widget;

import java.awt.*;

/**
 * @author ImXianyu
 * @since 6/17/2023 10:02 AM
 */
public class MusicSpectrum extends Widget {

    public ModeSetting<Style> style = new ModeSetting<>("Style", Style.Rect);

    public enum Style {
        Rect,
        BlurredRect,
        Lines,

    }

    public ColorSetting lineColor = new ColorSetting("Lines Color", new HSBColor(255, 255, 255, 255), () -> this.style.getValue() == Style.Lines);

    public NumberSetting<Double> multiplier = new NumberSetting<>("Multiplier", 1.0, 0.1, 2.0, 0.1);
    float[] renderSpectrum = new float[AudioPlayer.spectrumChannels];



    @Handler
    public void onRender(Render2DEvent.Render2DBeforeInventoryEvent event) {

        float offset = 170;

        if (CloudMusic.player != null) {
            double spectrumWidth = RenderSystem.getWidth() / ((double) CloudMusic.player.spectrum.length);
            double maximumSpectrum = 1;

            if (this.style.getValue() == Style.Lines) {

                RenderSystem.color(lineColor.getRGB(0));

                GL11.glDisable(GL11.GL_TEXTURE_2D);

                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glLineWidth(2.0f);

                GL11.glBegin(GL11.GL_LINE_STRIP);

                GL11.glVertex2d(0, RenderSystem.getHeight());
            }

            for (int i = 0; i < CloudMusic.player.spectrum.length; i++) {
                float sp = CloudMusic.player.spectrum[i];

                float target = sp * 1000 * (2 - multiplier.getValue().floatValue());

                if (CloudMusic.player.player.getStatus() != MediaPlayer.Status.PLAYING) {
                    target = offset * -1000;
                }

                renderSpectrum[i] = AnimationSystem.interpolate(renderSpectrum[i] * 1000, target, 0.4f) / 1000.0f;

/*                this.doRenderSpectrum(i, spectrumWidth, offset);

                maximumSpectrum = Math.max(maximumSpectrum, renderSpectrum[i] + offset);*/
            }

            for (int i = 0; i < CloudMusic.player.spectrum.length; i++) {
                this.doRenderSpectrum(i, spectrumWidth, offset);

                maximumSpectrum = Math.max(maximumSpectrum, renderSpectrum[i] + offset);
            }

            if (this.style.getValue() == Style.Lines) {
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                RenderSystem.resetColor();
            }

//            RenderSystem.color(-1);
//
//            GL11.glDisable(GL11.GL_TEXTURE_2D);
//
//            GL11.glBegin(GL11.G);
//
//            GL11.glVertex2d(0, RenderSystem.getHeight());
//
//            for (int i = 0; i < CloudMusic.player.spectrum.length; i++) {
//                if (i > 0) {
//                    GL11.glVertex2d(spectrumWidth * (i - 1) + spectrumWidth * 0.5, RenderSystem.getHeight() + (-renderSpectrum[i - 1] - offset));
//                }
//
//                GL11.glVertex2d(spectrumWidth * i + spectrumWidth * 0.5, RenderSystem.getHeight() + (-renderSpectrum[i] - offset));
//            }
//
//            GL11.glVertex2d(RenderSystem.getWidth(), RenderSystem.getHeight());
//
//            GL11.glEnd();
//            GL11.glEnable(GL11.GL_TEXTURE_2D);

//            if (CloudMusic.player.player.getStatus() != MediaPlayer.Status.PLAYING) {
//                for (int i = 0; i < CloudMusic.player.spectrum.length; i++) {
//                    renderSpectrum[i] = AnimationSystem.interpolate(renderSpectrum[i] * 1000, offset * -1000, 0.2f) / 1000.0f;
//                    ShaderUtils.doRectBlur(spectrumWidth * i, RenderSystem.getHeight(), spectrumWidth, -renderSpectrum[i] - offset);
////                    Rect.draw(spectrumWidth * i, RenderSystem.getHeight(), spectrumWidth, -renderSpectrum[i] - offset, new Color(125, 125, 125, 160).getRGB(), Rect.RectType.EXPAND);
//                }
//            } else {
//
//            }

            double range = MathUtils.range(1.1, 0.9, 1 + (maximumSpectrum - 100) * 0.002083);

            WidgetsManager.music.fftScale = AnimationSystem.interpolate(WidgetsManager.music.fftScale, range, 0.6);

        }

        this.setY(100000);
        this.setWidth(10);
        this.setHeight(10);
    };

    private void doRenderSpectrum(int i, double spectrumWidth, float offset) {

        switch (this.style.getValue()) {

            case Rect -> {
                Rect.draw(spectrumWidth * i, RenderSystem.getHeight(), spectrumWidth, -renderSpectrum[i] - offset, new Color(125, 125, 125, 160).getRGB(), Rect.RectType.EXPAND);
            }

            case BlurredRect -> {
                ShaderUtils.doRectBlur(spectrumWidth * i, RenderSystem.getHeight(), spectrumWidth, (-renderSpectrum[i] - offset));
            }

            case Lines -> {

                int i1 = 1;

                RenderSystem.color(lineColor.getRGB((i - 1) * i1));

                if (i > 0) {
                    GL11.glVertex2d(spectrumWidth * (i - 1) + spectrumWidth * 0.5, RenderSystem.getHeight() + (-renderSpectrum[i - 1] - offset));
                }

                RenderSystem.color(lineColor.getRGB(i * i1));

                GL11.glVertex2d(spectrumWidth * i + spectrumWidth * 0.5, RenderSystem.getHeight() + (-renderSpectrum[i] - offset));


            }

        }

    }

    public MusicSpectrum() {
        super("Music Spectrum");
    }

    @Override
    public void onRender(Render2DEvent event, boolean editing) {

    }
}
