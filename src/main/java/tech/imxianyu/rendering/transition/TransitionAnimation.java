package tech.imxianyu.rendering.transition;

import lombok.Getter;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.utils.dev.DevUtils;

/**
 * the transition animation for the guis
 * @author ImXianyu
 * @since 6/20/2023 7:15 PM
 */
public class TransitionAnimation {

    static float screeMaskAlpha = 0;
    static boolean increasing = false, running = false;
    static Task task;

    /**
     * render the transition mask on top of the screen
     */
    public static void render() {

        screeMaskAlpha = AnimationSystem.interpolate(screeMaskAlpha * 255, (increasing ? 1 : 0) * 255, (increasing ? 0.185f : 0.2f)) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;

        if (/*task == null || */(!increasing && screeMaskAlpha < 0.01))
            return;

        Rect.draw(
                0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RenderSystem.hexColor(0, 0, 0, (int) (screeMaskAlpha * 255)), Rect.RectType.EXPAND
        );

        if (increasing && screeMaskAlpha > 0.99) {
            increasing = false;
            task.run();
            task = null;
            running = false;
        }

    }

    public static void task(Runnable runnable) {

        if (running) {
            DevUtils.printCurrentInvokeStack();
            throw new IllegalStateException("Already Running!");
        }

        running = true;

        increasing = true;

        if (runnable == null) {
            System.err.println("Runnable == NULL!");
            DevUtils.printCurrentInvokeStack();
        }

        task = new Task(DevUtils.getCurrentInvokeStack()) {
            @Override
            public void run() {
                if (runnable != null)
                    runnable.run();
                else {
                    System.err.println("Runnable == NULL!");
                    System.out.println(this.getMsg());
                }
            }
        };
    }

    private abstract static class Task {

        @Getter
        private final String msg;
        public Task(String msg) {
            this.msg = msg;
        }

        public abstract void run();

    }
}
