package tech.imxianyu.rendering.loadingscreen.impl;

import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.loadingscreen.LoadingScreenRenderer;
import tech.imxianyu.rendering.loadingscreen.ZephyrLoadingScreen;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.awt.*;

/**
 * @author ImXianyu
 * @since 4/24/2023 6:25 PM
 */
public class NormalLoadingScreen extends LoadingScreenRenderer {


    double progressWidth = 0;

    double pbWidth;
    double pbHeight;
    double pbOffsetY;

    Image clientLogo;

    @Override
    public void init() {
        super.init();
        this.clientLogo = new Image(new ResourceLocation("Zephyr/textures/logo_128x.png"), 0, 0, 190, 190, Image.Type.Normal);

    }

    @Override
    public void render(int width, int height) {
        pbWidth = width - 80;
        pbHeight = 20;
        pbOffsetY = height * 0.8576923076923077;
        progressWidth = AnimationSystem.interpolate(progressWidth, pbWidth * MathHelper.clamp_double(ZephyrLoadingScreen.progress / 100F, 0, 1), 0.2f);

        Rect.draw(0, 0, width, height, RenderSystem.hexColor(25, 24, 26), Rect.RectType.ABSOLUTE_POSITION);

        /*BloomRenderer.preRenderShadow();
        RoundedRect.drawRound(width / 2.0d - pbWidth / 2.0, pbOffsetY, pbWidth,
                pbHeight, 5, Color.WHITE);
        BloomRenderer.postRenderShadow();*/

        RoundedRect.drawRound(width / 2.0d - pbWidth / 2.0, pbOffsetY, pbWidth,
                pbHeight, 5, new Color(128, 128, 128));

        RoundedRect.drawGradientHorizontal(width / 2.0d - pbWidth / 2.0, pbOffsetY, progressWidth,
                pbHeight, 5, new Color(63, 81, 255), new Color(129, 204, 255));


        this.clientLogo.setX(width / 2.0d - this.clientLogo.getWidth() / 2.0d);
        this.clientLogo.setY(height / 5.0d);
        this.clientLogo.draw();


        if (ZephyrLoadingScreen.progress == 100 && progressWidth >= pbWidth * MathHelper.clamp_double(ZephyrLoadingScreen.progress / 100F, 0, 1) - 1) {
            ZephyrLoadingScreen.alpha = (AnimationSystem.interpolate(ZephyrLoadingScreen.alpha * 255, 255, 0.2f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT);
        }
    }

    @Override
    public boolean isLoadingScreenFinished() {
        return ZephyrLoadingScreen.progress == 100
                && progressWidth >= pbWidth * MathHelper.clamp_double(ZephyrLoadingScreen.progress / 100F, 0, 1) - 1
                && ZephyrLoadingScreen.alpha * 255 == 255;
    }
}
