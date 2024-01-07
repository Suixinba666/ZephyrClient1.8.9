package tech.imxianyu.utils.lyric;

import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.font.ZFontRenderer;

public class SlideUpFont {
    static ZFontRenderer font = FontManager.pf40Medium;
    static double animationY = -1;
    public static void render(String text, double x, double y, int color) {
        if (animationY == -1) {
            animationY = y;
        }
        animationY = AnimationSystem.interpolate(animationY, y, 0.15f);
        font.drawString(text, x, animationY, color);
    }
}
