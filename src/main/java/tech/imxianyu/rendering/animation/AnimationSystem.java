package tech.imxianyu.rendering.animation;

import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.awt.*;

/**
 * the animation system of the client
 * @author ImXianyu
 * @since 4/16/2023 6:20 AM
 */
public class AnimationSystem {
    /**
     * nano delta frame time for timing animations
     */
    private static long lastNanoFrame = System.nanoTime();

    /**
     * interpolate animation
     * @param startValue start
     * @param endValue end
     * @param fraction speed
     * @return animation value
     */
    public static float interpolate(float startValue, float endValue,
                                    float fraction) {

        boolean increasing = startValue < endValue;

        double result = startValue + (endValue - startValue) * interpolateCurve(fraction) * RenderSystem.getFrameDeltaTime() * 100;

        if (increasing) {
            return (float) Math.min(endValue, result);
        } else {
            return (float) Math.max(endValue, result);
        }
    }

    /**
     * interpolate animation
     * @param startValue start
     * @param endValue end
     * @param fraction speed
     * @return animation value
     */
    public static double interpolate(double startValue, double endValue,
                                     double fraction) {
        boolean increasing = startValue < endValue;

        double result = startValue + (endValue - startValue) * interpolateCurve(fraction) * RenderSystem.getFrameDeltaTime() * 100;

        if (increasing) {
            return Math.min(endValue, result);
        } else {
            return Math.max(endValue, result);
        }
    }

    /**
     * interpolate animation with approx (percent > 0.99)
     * @param startValue start
     * @param endValue end
     * @param fraction speed
     * @return animation value
     */
    public static double interpolateApprox(double startValue, double endValue,
                                           double fraction) {
        if (Math.abs(startValue / endValue) > 0.99) {
            return endValue;
        }

        boolean increasing = startValue < endValue;

        double result = startValue + (endValue - startValue) * interpolateCurve(fraction) * RenderSystem.getFrameDeltaTime() * 100;

        if (increasing) {
            return Math.min(endValue, result);
        } else {
            return Math.max(endValue, result);
        }
    }

    /**
     * the curve of the interpolate animation
     * @param t speed input
     * @return curve value
     */
    private static strictfp float interpolateCurve(float t) {
        float clampValue;

        if (t < 0.2f) {
            // 3.125 * t²
            clampValue = 3.125f * (t * t);
        } else if (t > 0.8f) {
            // -3.125 * t² + 6.25 * t - 2.125
            clampValue = -3.125f * (t * t) + 6.25f * t - 2.125f;
        } else {
            // 1.25 * (t - 0.1)
            clampValue = 1.25f * (t - 0.1f);
        }


        return clamp(clampValue);
    }

    /**
     * the curve of the interpolate animation
     * @param t speed input
     * @return curve value
     */
    private static strictfp double interpolateCurve(double t) {
        double clampValue;

        if (t < 0.2) {
            // 3.125 * t²
            clampValue = 3.125 * StrictMath.pow(t, 2);
        } else if (t > 0.8f) {
            // -3.125 * t² + 6.25 * t - 2.125
            clampValue = -3.125 * StrictMath.pow(t, 2) + 6.25 * t - 2.125;
        } else {
            // 1.25 * (t - 0.1)
            clampValue = 1.25 * (t - 0.1);
        }

        return clamp(clampValue);
    }

    private static double clamp(double t) {
        return (t < 0.0d) ? 0.0d : Math.min(t, 1.0d);
    }

    private static float clamp(float t) {
        return (t < 0.0f) ? 0.0f : Math.min(t, 1.0f);
    }

    /**
     * calculates the delta frame time
     */
    public static void calcFrameDelta() {

        RenderSystem.setFrameDeltaTime(((double) System.nanoTime() - (double) lastNanoFrame) / 1000000000.0);
        lastNanoFrame = System.nanoTime();
    }

    public static Color getColorAnimationState(Color animation, Color finalState, double speed) {
        float add = (float) (RenderSystem.getFrameDeltaTime() * 10 * speed);
        float animationr = animation.getRed();
        float animationg = animation.getGreen();
        float animationb = animation.getBlue();
        float finalStater = finalState.getRed();
        float finalStateg = finalState.getGreen();
        float finalStateb = finalState.getBlue();
        float finalStatea = finalState.getAlpha();
        //r
        if (animationr < finalStater) {
            if (animationr + add < finalStater)
                animationr += add;
            else
                animationr = finalStater;
        } else {
            if (animationr - add > finalStater)
                animationr -= add;
            else
                animationr = finalStater;
        }
        //g
        if (animationg < finalStateg) {
            if (animationg + add < finalStateg)
                animationg += add;
            else
                animationg = finalStateg;
        } else {
            if (animationg - add > finalStateg)
                animationg -= add;
            else
                animationg = finalStateg;
        }
        //b
        if (animationb < finalStateb) {
            if (animationb + add < finalStateb)
                animationb += add;
            else
                animationb = finalStateb;
        } else {
            if (animationb - add > finalStateb)
                animationb -= add;
            else
                animationb = finalStateb;
        }
        animationr /= 255.0f;
        animationg /= 255.0f;
        animationb /= 255.0f;
        finalStatea /= 255.0f;
        if (animationr > 1.0f) animationr = 1.0f;
        if (animationg > 1.0f) animationg = 1.0f;
        if (animationb > 1.0f) animationb = 1.0f;
        if (finalStatea > 1.0f) finalStatea = 1.0f;
        return new Color(animationr, animationg, animationb, finalStatea);
    }

    public static float getAnimationState(float animation, float finalState, float speed) {
        float add = (float) (RenderSystem.getFrameDeltaTime() * 10 * speed);
        if (animation < finalState) {
            if (animation + add < finalState)
                animation += add;
            else
                animation = finalState;
        } else {
            if (animation - add > finalState)
                animation -= add;
            else
                animation = finalState;
        }
        return animation;
    }
}
