package tech.imxianyu.gui.mainmenu.impl;

import lombok.Getter;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.imxianyu.Zephyr;
import tech.imxianyu.gui.alt.GuiAltManager;
import tech.imxianyu.gui.mainmenu.ZephyrMainMenu;
import tech.imxianyu.gui.multiplayer.ZephyrMultiPlayerUI;
import tech.imxianyu.gui.multiplayerdark.ZephyrMultiPlayerUIDark;
import tech.imxianyu.gui.selectworld.ZephyrSelectWorld;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.entities.impl.GradientButton;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.entities.impl.TextLabel;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.BloomRenderer;
import tech.imxianyu.rendering.transition.TransitionAnimation;
import tech.imxianyu.utils.timing.Timer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ImXianyu
 * @since 4/15/2023 8:47 PM
 */
public class NormalMainMenu extends ZephyrMainMenu {

    @Getter
    private static final NormalMainMenu instance = new NormalMainMenu();

    TextLabel clientTag;
    Timer clientTagTimer = new Timer();
    Image clientLogo;

    List<GradientButton> buttons = new ArrayList<>();

    float screenMaskAlpha = 1;
    boolean firstFrame = false;
    Timer fpsTimer = new Timer();

    List<String> changeLog = Arrays.asList(
            "Change log for " + Formatting.GOLD + Zephyr.getInstance().getVersion().getType().name() + " " + Formatting.GREEN + Zephyr.getInstance().getVersion().getBuildDate() + Formatting.RESET + ": ",
            Formatting.GREEN + "[!] Changed client font",
            Formatting.GREEN + "[+] Redesigned multiplayer UI",
            Formatting.GREEN + "[+] Japanese songs can display Roman accent lyrics",
            Formatting.GREEN + "[+] Added scoreboard widget",
            Formatting.GREEN + "[+] Widgets can now be magnetically snapped to the sides of windows"
    );

    public NormalMainMenu() {

    }

    @Override
    public void initGui() {

        this.clientTag = new TextLabel("Zephyr", FontManager.gsans40, -200, 30 + 32 - FontManager.gsans40.getHeight() / 2.0, ColorUtils.getColor(ColorUtils.ColorType.Text));
        this.clientLogo = new Image(new ResourceLocation("Zephyr/textures/logo_128x.png"), -264, 30, 64, 64, Image.Type.Normal);

        this.buttons.clear();

        List<Tuple<String, Runnable>> buttonNameAndActions = new ArrayList<>();

        buttonNameAndActions.add(
            new Tuple<>(
                "Single Player",
                () -> {
                    if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
                        TransitionAnimation.task(() -> mc.displayGuiScreen(new ZephyrSelectWorld(this)));
                    } else {
                        TransitionAnimation.task(() -> mc.displayGuiScreen(new GuiSelectWorld(this)));
                    }
                }
            )
        );

        buttonNameAndActions.add(new Tuple<>("Multi Player", () -> {
            // no longer experimental so we just need to display the new ui
            if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
                TransitionAnimation.task(() -> mc.displayGuiScreen(new ZephyrMultiPlayerUI(this)));
            } else {
                TransitionAnimation.task(() -> mc.displayGuiScreen(new ZephyrMultiPlayerUIDark(this)));

            }
        }));

        buttonNameAndActions.add(new Tuple<>("Alt Manager", () -> {
            TransitionAnimation.task(() -> mc.displayGuiScreen(new GuiAltManager(this)));
        }));

        buttonNameAndActions.add(new Tuple<>("Game Settings ", () -> {
            TransitionAnimation.task(() -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)));
        }));

        buttonNameAndActions.add(new Tuple<>("Quit", () -> {
            TransitionAnimation.task(() -> mc.shutdown());
        }));

        double offsetY = 160;
        for (Tuple<String, Runnable> tuple : buttonNameAndActions) {
            this.buttons.add(new GradientButton(tuple.getFirst(), -300, offsetY, 150, 20, 3, tuple.getSecond()));

            offsetY += 50;
        }
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {

        if (!firstFrame) {
            firstFrame = true;
            RenderSystem.setFrameDeltaTime(0);

            this.clientTagTimer.reset();
        }

        ZFontRenderer fontRenderer = FontManager.pf18;

        double width = RenderSystem.getWidth();
        double height = RenderSystem.getHeight();


//        retroBackground.draw(0, 0, mouseX, mouseY);

        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RenderSystem.hexColor(28, 28, 28), Rect.RectType.EXPAND);

        this.clientTag.setFontRenderer(FontManager.gsans40);
        this.clientTag.setColor(ColorUtils.getColor(ColorUtils.ColorType.Text));

        double widgetsDestX = 50;

        if (this.clientTagTimer.isDelayed(500)) {
            screenMaskAlpha = AnimationSystem.interpolate(screenMaskAlpha * 255, 0, 0.15f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
        }

        if (this.clientTagTimer.isDelayed(1000)) {
            this.clientLogo.setX(AnimationSystem.interpolate(this.clientLogo.getX(), widgetsDestX, 0.1f));
            this.clientTag.setX(AnimationSystem.interpolate(this.clientTag.getX(), widgetsDestX + 64, 0.1f));
        }

        this.clientLogo.draw();
        this.clientTag.draw();

        double buttonDestX = widgetsDestX + 10;

        BloomRenderer.preRenderShadow();

        for (GradientButton button : this.buttons) {
            RoundedRect.drawRound(button.getX(), button.getY() - button.yAnimation, button.getWidth(), button.getHeight(), button.getRadius(), new Color(ColorUtils.getColor(ColorUtils.ColorType.Base)));
        }

        BloomRenderer.postRenderShadow();


        for (int i = 0; i < this.buttons.size(); i++) {
            GradientButton button = this.buttons.get(i);

            button.draw(mouseX, mouseY);

            if (this.clientTagTimer.isDelayed(1000)) {
                if (i == 0) {
                    button.setX(AnimationSystem.interpolate(button.getX(), buttonDestX, 0.1f));
                }

                if (i > 0) {
                    GradientButton lastButton = this.buttons.get(i - 1);

                    if (lastButton.getX() + lastButton.getWidth() >= buttonDestX / 2.0)
                        button.setX(AnimationSystem.interpolate(button.getX(), buttonDestX, 0.1f));
                    else
                        break;
                }
            }
        }

        this.renderInfo();

        if (RenderSystem.isHovered(mouseX, mouseY, 0, RenderSystem.getHeight() - 1 - fontRenderer.getHeight(), fontRenderer.getStringWidth(this.getInfoLeft().get(0)), fontRenderer.getHeight() + 1) && Mouse.isButtonDown(0) && !mouseClicked) {
            mouseClicked = true;
            renderChangeLog = !renderChangeLog;
        }

        if (!Mouse.isButtonDown(0) && mouseClicked)
            mouseClicked = false;

        if (renderChangeLog) {
            double maxWidth = 0;

            for (String s : changeLog) {
                maxWidth = Math.max(fontRenderer.getStringWidth(s), maxWidth);
            }

            fontRenderer.drawString(String.join("\n", changeLog), RenderSystem.getWidth() - maxWidth - 20, 50, -1);
        }

        Rect.draw(0, 0, width, height, CloudMusic.player != null ? RenderSystem.hexColor(255, 255, 255, (int) (screenMaskAlpha * 255)) : RenderSystem.hexColor(0, 0, 0, (int) (screenMaskAlpha * 255)), Rect.RectType.EXPAND);
    }

    private boolean mouseClicked = false, renderChangeLog = false;

    private void renderInfo() {

        ZFontRenderer fontRenderer = FontManager.pf18;
        double offsetY = RenderSystem.getHeight() - fontRenderer.getHeight() - 1;

        for (String s : this.getInfoLeft()) {
            fontRenderer.drawOutlineString(s, 2, offsetY, -1, RenderSystem.hexColor(0, 0, 0));
            offsetY -= fontRenderer.getHeight();
        }

        offsetY = RenderSystem.getHeight() - fontRenderer.getHeight() - 1;

        for (String s : this.getInfoRight()) {
            fontRenderer.drawOutlineString(s, RenderSystem.getWidth() - fontRenderer.getStringWidth(s) - 2, offsetY, -1, RenderSystem.hexColor(0, 0, 0));
            offsetY -= fontRenderer.getHeight();
        }
    }

    private List<String> getInfoLeft() {

        return Arrays.asList(
                String.format("Zephyr %sBuild %s%s %s%s", Formatting.GRAY, Formatting.GOLD, Zephyr.getInstance().getVersion().getType().name(), Formatting.GREEN, Zephyr.getInstance().getVersion().getBuildDate())
        );

    }

    private List<String> getInfoRight() {
        return Arrays.asList(
                Formatting.RED + "Warning: " + Formatting.RESET + "Public Beta version, does not represent final quality"
        );
    }
}
