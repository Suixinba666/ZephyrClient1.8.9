package net.minecraft.util;

import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;
import tech.imxianyu.eventapi.EventBus;
import tech.imxianyu.events.input.MouseXYChangeEvent;

public class MouseHelper {
    /**
     * Mouse delta X this frame
     */
    public int deltaX;

    /**
     * Mouse delta Y this frame
     */
    public int deltaY;

    /**
     * Grabs the mouse cursor it doesn't move and isn't seen.
     */
    public void grabMouseCursor() {
//        Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
        Mouse.setGrabbed(true);
        this.deltaX = 0;
        this.deltaY = 0;
    }

    /**
     * Ungrabs the mouse cursor so it can be moved and set it to the center of the screen
     */
    public void ungrabMouseCursor() {
        Mouse.setGrabbed(false);
        Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);

    }

    public void mouseXYChange() {
        //CLIENT
        MouseXYChangeEvent event = EventBus.call(new MouseXYChangeEvent(Mouse.getDX(), Mouse.getDY()));
        //END CLIENT
        this.deltaX = event.deltaX;
        this.deltaY = event.deltaY;
    }
}
