package tech.imxianyu.rendering.loadingscreen.impl;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.Zephyr;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.loadingscreen.LoadingScreenRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.utils.timing.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenshinImpactLoadingScreen extends LoadingScreenRenderer {


    FadeInOutImage gs1, gs2;

    Timer startTimer = new Timer();
    boolean firstFrame = false;

    @Override
    public void init() {
        super.init();

        gs1 = new FadeInOutImage(new ResourceLocation("Zephyr/textures/loadingscreen/genshin/gs1.png"));
        gs2 = new FadeInOutImage(new ResourceLocation("Zephyr/textures/loadingscreen/genshin/gs2.png"));

        CloudMusic.initialize("");
        Zephyr.getInstance().initJavaFX();
        CloudMusic.play(Collections.singletonList(CloudMusic.music(1455706958)));
    }

    @Override
    public void render(int width, int height) {
        Rect.draw(0, 0, width, height, RenderSystem.hexColor(255, 255, 255), Rect.RectType.ABSOLUTE_POSITION);

        if (!firstFrame) {
            firstFrame = true;
            startTimer.reset();
        }

        if (!startTimer.isDelayed(1000))
            return;

        if (!gs1.isFinished())
            gs1.render(width, height);
        else
            gs2.render(width, height);
    }

    @Override
    public boolean isLoadingScreenFinished() {
        return gs1.isFinished() && gs2.isFinished();
    }

    private static class FadeInOutImage {

        @Getter
        private final ResourceLocation img;

        float screeMaskAlpha = 0;
        boolean increasing = true;

        boolean finished = false;

        boolean firstFrame = false;

        Timer timer = new Timer();

        public FadeInOutImage(ResourceLocation loc) {
            img = loc;
        }

        public void render(int width, int height) {

            if (!firstFrame) {
                firstFrame = true;
                timer.reset();
            }

            if (increasing || timer.isDelayed(2000)) {
                screeMaskAlpha += increasing ? 1 * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT : -1 * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
            }

            if ((!increasing && screeMaskAlpha < 0.01))
                finished = true;

            if (increasing && screeMaskAlpha > 0.99) {
                increasing = false;
                timer.reset();
            }

            GL11.glColor4f(1, 1, 1, screeMaskAlpha);
            Image.draw(img, 0, 0, width, height, Image.Type.NoColor);

        }

        public boolean isFinished() {
            return finished;
        }
    }
}
