package tech.imxianyu.widget.impl.keystrokes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjglx.input.Keyboard;
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

public class Key {

    private final KeyBinding key;
    private final double xOffset, yOffset, width, height;

    private int pressedAlpha = 0;

    List<Circle> circles = new ArrayList<>();

    public Key(KeyBinding key, double xOffset, double yOffset, double width, double height) {
        this.key = key;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
    }

    public void render(double x, double y) {
//        TexturedShadow.drawShadow(x + xOffset, y + yOffset, width, height, 1.0f, 3);

        if (this.key.isPressed()) {
            this.circles.add(new Circle());
        }

        this.pressedAlpha = (int) AnimationSystem.interpolate(this.pressedAlpha, this.key.pressed ? 80 : 0, 0.2f);

//        if (ZephyrSettings.reduceShaders.getValue()) {
//            int color = RenderSystem.hexColor(20, 20, 20);
//
//            Rect.draw(x + xOffset, y + yOffset, width, height, color, Rect.RectType.EXPAND);
//
//
//            Rect.draw(x + xOffset, y + yOffset, width, height, RenderSystem.reAlpha(RenderSystem.getOppositeColorHex(color), this.pressedAlpha * 0.003921569f), Rect.RectType.EXPAND);
//
//        } else {
//
//        }
//
//        ZFontRenderer fontRenderer = FontManager.shs18;
//
//        if (!this.getKeyName().equals("SPACE")) {
//            fontRenderer.drawString(this.getKeyName(), x + xOffset + 3, y + yOffset + 3, RenderSystem.getOppositeColorHex(color));
//            fontRenderer.drawString(this.getKeyName(), x + xOffset + 3, y + yOffset + 3, RenderSystem.reAlpha(color, this.pressedAlpha * 0.003921569f));
//        }


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

        ZFontRenderer fontRenderer = FontManager.pf18;

        if (!this.getKeyName().equals("SPACE")) {
            fontRenderer.drawString(this.getKeyName(), x + xOffset + 3, y + yOffset + 3, -1);
        }

    }

    private String getKeyName() {
        return Keyboard.getKeyName(this.key.getKeyCode());
    }
}
