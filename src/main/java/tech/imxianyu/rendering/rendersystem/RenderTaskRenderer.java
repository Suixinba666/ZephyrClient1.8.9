package tech.imxianyu.rendering.rendersystem;

import org.lwjgl.opengl.GL11;

/**
 * @author ImXianyu
 * @since 4/15/2023 9:27 PM
 */
public class RenderTaskRenderer {

    public Runnable task;

    public boolean matrixStack, attribStack, doScale, doTranslate;

    public double scaleX, scaleY, translateX, translateY;

    public boolean doingTask = false;

    public RenderTaskRenderer task(Runnable task) {
        this.doingTask = true;
        this.task = task;
        return this;
    }

    public RenderTaskRenderer matrixStack() {
        this.matrixStack = true;
        return this;
    }

    public RenderTaskRenderer attribStack() {
        this.attribStack = true;
        return this;
    }

    public RenderTaskRenderer scale(double scaleX, double scaleY) {
        this.doScale = true;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    public RenderTaskRenderer translate(double translateX, double translateY) {
        this.doTranslate = true;
        this.translateX = translateX;
        this.translateY = translateY;
        return this;
    }

    public void doTask() {
        if (this.task == null) {
            throw new NullPointerException("task == null!");
        }

        if (this.doingTask) {
            throw new IllegalStateException("Already Doing Task!");
        }

        if (matrixStack)
            GL11.glPushMatrix();

        if (attribStack)
            GL11.glPushAttrib(8256);

        if (doTranslate)
            GL11.glTranslated(this.translateX, this.translateY, 0);

        if (doScale)
            GL11.glScaled(this.scaleX, this.scaleY, 0);

        this.task.run();

        if (doScale)
            GL11.glScaled(1 / scaleX, 1 / scaleY, 0);

        if (doTranslate)
            GL11.glTranslated(-this.translateX, -this.translateY, 0);

        if (attribStack)
            GL11.glPopAttrib();

        if (matrixStack)
            GL11.glPopMatrix();

    }

    public void reset() {
        this.task = null;
        this.doingTask = this.doScale = this.doTranslate = this.matrixStack = this.attribStack = false;
    }
}
