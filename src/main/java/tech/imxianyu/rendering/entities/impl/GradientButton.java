package tech.imxianyu.rendering.entities.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Formatting;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.entities.RenderableEntity;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.awt.*;

/**
 * @author ImXianyu
 * @since 4/24/2023 7:43 PM
 */
public class GradientButton extends RenderableEntity {

    public double yAnimation = 0;
    @Getter
    @Setter
    private String label;
    @Getter
    @Setter
    private Runnable onClick;
    private double hoveredWidth = 0;
    private int hoveredAlpha = 0;

    @Getter
    @Setter
    private int radius;

    private boolean mouseDown = false, pressed = false;

    public GradientButton(String label, double x, double y, double width, double height, int radius, Runnable onClick) {
        super(x, y, width, height);

        this.setLabel(label);

        this.setRadius(radius);
        this.setOnClick(onClick);

    }

    public void draw(double mouseX, double mouseY) {

        if (!Mouse.isButtonDown(0) && mouseDown)
            mouseDown = false;

        RoundedRect.drawRound(this.getX(), this.getY() - yAnimation, this.getWidth(), this.getHeight(), this.getRadius(), Color.BLACK);

        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, this.getX(), this.getY() - yAnimation, this.getWidth(), this.getHeight(), -this.getRadius() * 0.5);

        if (hovered && Mouse.isButtonDown(0) && !mouseDown) {
            mouseDown = true;
            this.onClick.run();
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) && Keyboard.isKeyDown(Keyboard.getKeyIndex(this.getLabel().substring(0, 1))) && !pressed) {
            pressed = true;
            this.onClick.run();
        }

        this.yAnimation = AnimationSystem.interpolate(this.yAnimation, hovered ? 2 : 0, 0.2f);

        this.hoveredWidth = (AnimationSystem.interpolate(hoveredWidth, hovered ? this.getWidth() : 0, 0.2f));
        this.hoveredAlpha = (int) AnimationSystem.interpolate(this.hoveredAlpha, hovered ? 255 : 0, 0.15f);

        if (this.hoveredWidth > 1) {
            RoundedRect.drawGradientHorizontal(this.getX(), this.getY() - yAnimation, this.hoveredWidth, this.getHeight(), this.getRadius(), new Color(63, 81, 255, hoveredAlpha), new Color(129, 204, 255, hoveredAlpha));
        }

        ZFontRenderer labelRenderer = FontManager.pf18;

        labelRenderer.drawString(this._getLabel(), this.getX() + 10, this.getY() + this.getHeight() / 2.0 - labelRenderer.getHeight() / 2.0 - yAnimation, ColorUtils.getColor(ColorUtils.ColorType.Text));

    }

    private String _getLabel() {

        String lbl = this.getLabel();

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            return Formatting.UNDERLINE + lbl.substring(0, 1) + Formatting.RESET + lbl.substring(1);
        }

        return lbl;
    }
}
