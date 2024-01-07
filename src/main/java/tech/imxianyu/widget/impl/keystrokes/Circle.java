package tech.imxianyu.widget.impl.keystrokes;

import tech.imxianyu.rendering.rendersystem.RenderSystem;

public class Circle {

    public double length = 0;
    public int alpha = 80;

    public void draw(double posX, double posY) {
        RenderSystem.circle(posX, posY, length, RenderSystem.hexColor(255, 255, 255, alpha));
    }

}
