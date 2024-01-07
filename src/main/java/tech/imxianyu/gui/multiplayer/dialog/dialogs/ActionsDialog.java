package tech.imxianyu.gui.multiplayer.dialog.dialogs;

import net.minecraft.util.Tuple;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.imxianyu.gui.multiplayer.ZephyrMultiPlayerUI;
import tech.imxianyu.gui.multiplayer.dialog.Dialog;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.awt.*;
import java.util.List;

public class ActionsDialog extends Dialog {

    private final String title, content;

    private double openCloseScale = 1.1;

    private final List<Tuple<String, Runnable>> buttons;

    public ActionsDialog(String title, String content, List<Tuple<String, Runnable>> actions) {

        this.title = title;
        this.content = content;

        this.buttons = actions;
    }

    @Override
    public void render(double mouseX, double mouseY, ZephyrMultiPlayerUI inst) {
        super.drawBackgroundMask(inst);
        this.openCloseScale = AnimationSystem.interpolate(this.openCloseScale, this.isClosing() ? 1.1 : 1, 0.3);

        ZFontRenderer titleRenderer = FontManager.segoe20;
        ZFontRenderer contentRenderer = FontManager.segoe18;

        int intAlpha = (int) (this.alpha * 255);

        double width = Math.max(250, titleRenderer.getStringWidth(this.title) + 60);

        List<String> strings = contentRenderer.wrapWords(this.content, width - 60);

        double height = 80 + contentRenderer.getHeight() * strings.size();
        double x = RenderSystem.getWidth() * 0.5 - width * 0.5,
                y = RenderSystem.getHeight() * 0.5 - height * 0.5;

        this.doGlPreTransforms(this.openCloseScale);

        RoundedRect.drawRound(x, y, width, height, 3, new Color(233, 233, 233, intAlpha));

        titleRenderer.drawString(this.title, x + 20, y + 17, RenderSystem.hexColor(0, 0, 0, intAlpha));

        contentRenderer.drawString(String.join("\n", strings), x + 20, y + 35, RenderSystem.hexColor(0, 0, 0, Math.min(160, intAlpha)));

        double buttonsSpacing = 15;
        double buttonsX = x + width - buttonsSpacing - contentRenderer.getStringWidth(buttons.get(0).getFirst());
        double buttonsY = y + height - 22;

        for (Tuple<String, Runnable> button : this.buttons) {
            contentRenderer.drawString(button.getFirst(), buttonsX, buttonsY, RenderSystem.hexColor(0, 0, 0, Math.min(160, intAlpha)));

            if (RenderSystem.isHovered(mouseX, mouseY, buttonsX, buttonsY, contentRenderer.getStringWidth(button.getFirst()), contentRenderer.getHeight(), -5)) {
                RoundedRect.drawRound(buttonsX, buttonsY, contentRenderer.getStringWidth(button.getFirst()), contentRenderer.getHeight(), 2, -5, new Color(23, 23, 23, Math.min(30, intAlpha)));

                if (Mouse.isButtonDown(0) && !previousMouse) {
                    previousMouse = true;
                    button.getSecond().run();
                    this.close();
                }

            }

            buttonsX = buttonsX - (contentRenderer.getStringWidth(button.getFirst()) + buttonsSpacing);
        }

        this.disposeTransforms();

        if (!Mouse.isButtonDown(0) && previousMouse) {
            previousMouse = false;
        }
    }

    public static Tuple<String, Runnable> buildAction(String label, Runnable onClick) {
        return new Tuple<>(label, onClick);
    }
}
