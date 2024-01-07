package tech.imxianyu.widget.impl;

import javafx.scene.media.MediaPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.music.IMusic;
import tech.imxianyu.rendering.Bloom;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.TexturedShadow;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.*;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.widget.Widget;

import java.awt.*;

/**
 * @author ImXianyu
 * @since 6/17/2023 10:03 AM
 */
public class Music extends Widget {
    double width = 380 / 2d;
    double height = 100 / 2d;
    double fftScale = 1;

    public BooleanSetting fftScaleSetting = new BooleanSetting("FFT Scale", false);


    public Music() {
        super("Music");
    }

    @Override
    public void onRender(Render2DEvent event, boolean editing) {



        IMusic playingMusic = CloudMusic.currentlyPlaying;

        boolean playing = playingMusic != null && CloudMusic.player != null && CloudMusic.player.player.getStatus() != MediaPlayer.Status.STOPPED;

        simpleStyleAlpha = AnimationSystem.interpolate(simpleStyleAlpha * 255, playing ? 255 : 0, playing ? 0.15f : 0.2f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;


        if (playingMusic != null) {

            ResourceLocation cover = Music.getMusicCover(playingMusic);

            if (mc.getTextureManager().getTexture(cover) != null) {


                double space = 4;
                double imgSize = 48;

                GlStateManager.pushMatrix();

                if (fftScaleSetting.getValue()) {
                    GlStateManager.translate(space + imgSize / 2, RenderSystem.getHeight() - space - imgSize / 2, 0);
                    GlStateManager.scale(fftScale, fftScale, 0);
                    GlStateManager.translate(-(space + imgSize / 2), -(RenderSystem.getHeight() - space - imgSize / 2), 0);
                }

                GL11.glColor4f(1, 1, 1, simpleStyleAlpha);
                Image.draw(cover, space, RenderSystem.getHeight() - space - imgSize, imgSize, imgSize, Image.Type.NoColor);
                TexturedShadow.drawShadow(space, RenderSystem.getHeight() - space - imgSize, imgSize, imgSize, simpleStyleAlpha);

                GlStateManager.popMatrix();

                if (!ZephyrSettings.reduceShaders.getValue()) {
                    Bloom.bloomBuffer.bindFramebuffer(true);
                    FontManager.pf25bold.drawString(playingMusic.getName(), space * 2 + imgSize, RenderSystem.getHeight() - space - imgSize, RenderSystem.hexColor(255, 255, 255, (int) (simpleStyleAlpha * 255)));
                    mc.getFramebuffer().bindFramebuffer(true);
                }


                FontManager.pf25bold.drawString(playingMusic.getName(), space * 2 + imgSize, RenderSystem.getHeight() - space - imgSize, RenderSystem.hexColor(255, 255, 255, (int) (simpleStyleAlpha * 255)));
                FontManager.pf20.drawString(playingMusic.getArtists(), space * 2 + imgSize, RenderSystem.getHeight() - space - imgSize + FontManager.pf25bold.getHeight(), RenderSystem.hexColor(255, 255, 255, (int) (simpleStyleAlpha * 125)));


                if (CloudMusic.player != null) {
                    int cMin = CloudMusic.player.getCurrentTimeSeconds() / 60;
                    int cSec = (CloudMusic.player.getCurrentTimeSeconds() - (CloudMusic.player.getCurrentTimeSeconds() / 60) * 60);
                    String currentTime = (cMin < 10 ? "0" + cMin : cMin) + ":" + (cSec < 10 ? "0" + cSec : cSec);
                    int tMin = CloudMusic.player.getTotalTimeSeconds() / 60;
                    int tSec = (CloudMusic.player.getTotalTimeSeconds() - (CloudMusic.player.getTotalTimeSeconds() / 60) * 60);
                    String totalTime = (tMin < 10 ? "0" + tMin : tMin) + ":" + (tSec < 10 ? "0" + tSec : tSec);

                    FontManager.pf20.drawString(currentTime + " - " + totalTime, space * 2 + imgSize, RenderSystem.getHeight() - space - FontManager.pf20.getHeight(), RenderSystem.hexColor(255, 255, 255, (int) (simpleStyleAlpha * 125)));
                }


            }


        }


    }

    float simpleStyleAlpha = 0.0f;


    public static ResourceLocation getMusicCover(IMusic music) {
        return new ResourceLocation("Zephyr/textures/MusicCover" + music.getId() + ".png");
    }

    public static ResourceLocation getMusicCoverLarge(IMusic music) {
        return new ResourceLocation("Zephyr/textures/MusicCoverLarge" + music.getId() + ".png");
    }


    private int cRange(int c) {
        if (c < 0) {
            c = 0;
        }

        if (c > 255) {
            c = 255;
        }

        return c;
    }
}
