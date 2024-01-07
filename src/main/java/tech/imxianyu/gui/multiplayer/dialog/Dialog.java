package tech.imxianyu.gui.multiplayer.dialog;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import tech.imxianyu.gui.multiplayer.ZephyrMultiPlayerUI;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

public abstract class Dialog {

    @Getter
    private boolean closing = false;
    private float maskAlpha = 0.0f;
    protected float alpha = 0.0f;

    public boolean previousMouse = true;



    public Dialog() {

    }

    public abstract void render(double mouseX, double mouseY, ZephyrMultiPlayerUI inst);

    protected void doGlPreTransforms(double scale) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(RenderSystem.getWidth() * 0.5, RenderSystem.getHeight() * 0.5, 0);
        GlStateManager.scale(scale, scale, 0);
        GlStateManager.translate(RenderSystem.getWidth() * -0.5, RenderSystem.getHeight() * -0.5, 0);
    }

    protected void disposeTransforms() {
        GlStateManager.popMatrix();
    }

    protected void drawBackgroundMask(ZephyrMultiPlayerUI inst) {
        this.maskAlpha = AnimationSystem.interpolate(this.maskAlpha, this.closing ? 0.0f : 0.6f, 0.2f);
        this.alpha = AnimationSystem.interpolate(this.alpha, this.closing ? 0.0f : 1f, 0.2f);

//        ShaderBlur.render(Minecraft.getMinecraft().getFramebuffer().);
        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RenderSystem.hexColor(0, 0, 0, (int) (this.maskAlpha * 255)), Rect.RectType.EXPAND);
    }

    public void close() {
        this.closing = true;
    }

    public boolean canClose() {
        return this.closing && this.maskAlpha < 0.1;
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        return false;
    }

    public void mouseClicked(int mX, int mY, int mouseButton) {

    }

    public void mouseReleased(int mX, int mY, int state) {

    }

}
