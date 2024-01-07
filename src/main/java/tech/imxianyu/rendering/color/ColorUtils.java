package tech.imxianyu.rendering.color;

import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.awt.*;

/**
 * @author ImXianyu
 * @since 4/24/2023 8:58 AM
 */
public class ColorUtils {

    private static final int base = RenderSystem.hexColor(28, 28, 28);
    private static final int text = RenderSystem.hexColor(255, 255, 255);

    public ColorUtils() {

    }

    public static int interpolateColor(int from, int to, float speed) {
        Color f = new Color(from);
        int r = f.getRed();
        int g = f.getGreen();
        int b = f.getBlue();
        int a = f.getAlpha();

        Color t = new Color(to);

        r = (int) AnimationSystem.interpolate(r, t.getRed(), speed);
        g = (int) AnimationSystem.interpolate(g, t.getGreen(), speed);
        b = (int) AnimationSystem.interpolate(b, t.getBlue(), speed);
        a = (int) AnimationSystem.interpolate(a, t.getAlpha(), speed);

        r = checkInRange(r);
        g = checkInRange(g);
        b = checkInRange(b);
        a = checkInRange(a);

        return RenderSystem.hexColor(r, g, b, a);
    }

    public static int checkInRange(int c) {
        if (c < 0) {
            c = 0;
        }

        if (c > 255) {
            c = 255;
        }

        return c;
    }

    public static int getColor(ColorType type) {
        switch (type) {
            default:
            case Base: {
                return base;
            }

            case Text: {
                return text;
            }
        }
    }

    public enum ColorType {
        Base,
        Text
    }
}
