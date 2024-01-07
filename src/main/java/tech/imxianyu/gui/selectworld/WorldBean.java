package tech.imxianyu.gui.selectworld;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.storage.SaveFormatComparator;
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
 * @since 5/1/2023 2:25 PM
 */
public class WorldBean extends RenderableEntity {

    @Getter
    private final SaveFormatComparator comparator;
    public double yAnimation = 0;
    @Getter
    @Setter
    private int radius;
    private double hoveredWidth = 0;
    private int hoveredAlpha = 0;
    private boolean mouseDown = false;

    public WorldBean(SaveFormatComparator comparator, double x, double y, double width, double height, int radius) {
        super(x, y, width, height);

        this.comparator = comparator;
        this.setRadius(radius);
    }

    public void draw(double mouseX, double mouseY) {
        RoundedRect.drawRound(this.getX(), this.getY() - yAnimation, this.getWidth(), this.getHeight(), this.getRadius(), new Color(ColorUtils.getColor(ColorUtils.ColorType.Base)));

        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, this.getX(), this.getY() - yAnimation, this.getWidth(), this.getHeight(), -this.getRadius() * 0.5);

        if (hovered && Mouse.isButtonDown(0) && !mouseDown) {
            mouseDown = true;
        }

        this.yAnimation = AnimationSystem.interpolate(this.yAnimation, hovered ? 2 : 0, 0.2f);

        this.hoveredWidth = (AnimationSystem.interpolate(hoveredWidth, hovered ? this.getWidth() : 0, 0.2f));
        this.hoveredAlpha = (int) AnimationSystem.interpolate(this.hoveredAlpha, hovered ? 255 : 0, 0.15f);

        if (this.hoveredWidth > 1) {
            RoundedRect.drawGradientHorizontal(this.getX(), this.getY() - yAnimation, this.hoveredWidth, this.getHeight(), this.getRadius(), new Color(63, 81, 255, hoveredAlpha), new Color(129, 204, 255, hoveredAlpha));
        }

        ZFontRenderer labelRenderer = FontManager.pf18;

        labelRenderer.drawString(this.comparator.getDisplayName(), this.getX() + 10, this.getY() + 5 - yAnimation, ColorUtils.getColor(ColorUtils.ColorType.Text));

    }
}
