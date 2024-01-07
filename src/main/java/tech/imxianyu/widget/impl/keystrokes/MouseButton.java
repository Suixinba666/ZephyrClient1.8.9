package tech.imxianyu.widget.impl.keystrokes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.Bloom;
import tech.imxianyu.rendering.Blur;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.Stencil;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.ZephyrSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MouseButton {
    private final KeyBinding key;
    private final double xOffset, yOffset, width, height;

    private int pressedAlpha = 0;

    public List<Circle> circles = new ArrayList<>();

    public MouseButton(KeyBinding key, double xOffset, double yOffset, double width, double height) {
        this.key = key;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
    }

    public void render(double x, double y) {
//        TexturedShadow.drawShadow(x + xOffset, y + yOffset, width, height, 1.0f, 3);
//        int color = RenderSystem.hexColor(20, 20, 20);
//        Rect.draw(x + xOffset, y + yOffset, width, height, color, Rect.RectType.EXPAND);
//
//        this.pressedAlpha = (int) AnimationSystem.interpolate(this.pressedAlpha, this.key.pressed ? 255 : 0, 0.25f);
//        Rect.draw(x + xOffset, y + yOffset, width, height, RenderSystem.reAlpha(RenderSystem.getOppositeColorHex(color), this.pressedAlpha * 0.003921569f), Rect.RectType.EXPAND);


        this.pressedAlpha = (int) AnimationSystem.interpolate(this.pressedAlpha, this.key.pressed ? 80 : 0, 0.25f);

        if (!ZephyrSettings.reduceShaders.getValue()) {
            Rect.draw(x + xOffset, y + yOffset, width, height, RenderSystem.hexColor(0, 0, 0, 50), Rect.RectType.EXPAND);

            ShaderUtils.doRectBlurAndBloom(x + xOffset, y + yOffset, width, height);

        } else {
            Rect.draw(x + xOffset, y + yOffset, width, height, RenderSystem.hexColor(0, 0, 0, 160), Rect.RectType.EXPAND);

        }

        Rect.draw(x + xOffset, y + yOffset, width, height, RenderSystem.hexColor(255, 255, 255, this.pressedAlpha), Rect.RectType.EXPAND);

        Stencil.write();
        Rect.draw(x + xOffset, y + yOffset, width, height, -1, Rect.RectType.EXPAND);
        Stencil.erase(true);
        Iterator<Circle> it = this.circles.iterator();

        while (it.hasNext()) {
            Circle circle = it.next();

            circle.length = AnimationSystem.interpolate(circle.length, width * 1.1, 0.12);

            circle.draw(x + xOffset + width * 0.5, y + yOffset + height * 0.5);

            if (circle.length >= width * 0.7)
                circle.alpha = (int) AnimationSystem.interpolate(circle.alpha, 0, 0.1);

            if (circle.length >= width * 0.95)
                it.remove();
        }

        Stencil.dispose();

        String stringToRender = this.getCPS() == 0 ? this.getKeyName() : this.getCPS() + " CPS";
        ZFontRenderer fontRenderer = FontManager.pf18;
        fontRenderer.drawCenteredString(stringToRender, x + xOffset + width / 2, y + yOffset + height / 2 - fontRenderer.getHeight() / 2.0, -1);
//        fontRenderer.drawCenteredString(stringToRender, x + xOffset + width / 2, y + yOffset + height / 2 - fontRenderer.getHeight() / 2.0, RenderSystem.reAlpha(color, this.pressedAlpha * 0.003921569f));
    }

    public int getCPS() {
        return this.key.getKeyCode() == -100 ? CPSUtils.getLeftCPS() : CPSUtils.getRightCPS();
    }

    public String getKeyName() {
        return this.key.getKeyCode() == -100 ? "LMB" : "RMB";
    }
}
