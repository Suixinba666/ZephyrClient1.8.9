package tech.imxianyu.gui.clickgui;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.imxianyu.gui.ZephyrScreen;
import tech.imxianyu.gui.clickgui.panel.Panel;
import tech.imxianyu.gui.clickgui.panel.impl.ModulesPanel;
import tech.imxianyu.gui.clickgui.panel.impl.MusicPanel;
import tech.imxianyu.gui.clickgui.panel.impl.SettingsPanel;
import tech.imxianyu.gui.clickgui.panel.impl.WidgetsPanel;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.TexturedShadow;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.animation.animations.AnimationFactory;
import tech.imxianyu.rendering.animation.animations.MultiEndpointAnimation;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.BloomRenderer;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ImXianyu
 * @since 5/1/2023 4:29 PM
 */
public class ZephyrClickGui extends ZephyrScreen {

    @Getter
    private static final ZephyrClickGui instance = new ZephyrClickGui();
    @Getter
    final List<Panel> panels = new ArrayList<>();
    @Getter
    final ModulesPanel modulesPanel;
    @Getter
    final WidgetsPanel widgetsPanel;
    @Getter
    final MusicPanel musicPanel;
    @Getter
    final SettingsPanel settingsPanel;
    public boolean lmbPressed = false, rmbPressed = false;
    double posX = 100, posY = 100;
    double width = 640, height = 360;
    double borderRadius = 5;
    double moveX = 0, moveY = 0;
    double moveXW = 0, moveYW = 0;
    boolean dragging = false;
    public Panel currentPanel;

    RenderValues renderValues = new RenderValues();

    MultiEndpointAnimation openCloseAnimation = AnimationFactory.create().withStartValue(0).withEndPoints(0, 1);

    Image clientLogo = new Image(new ResourceLocation("Zephyr/textures/logo_128x.png"), 0, 0, 32, 32, Image.Type.Normal);

    boolean isClosing = false;

    public ZephyrClickGui() {
        this.modulesPanel = new ModulesPanel();
        this.widgetsPanel = new WidgetsPanel();
        this.musicPanel = new MusicPanel();
        this.settingsPanel = new SettingsPanel();

        this.panels.addAll(Arrays.asList(this.modulesPanel, this.widgetsPanel, this.musicPanel, this.settingsPanel));

        this.panels.forEach(Panel::init);

        this.currentPanel = this.panels.get(0);
    }

    @Override
    public void initGui() {
        this.openCloseAnimation = AnimationFactory.create().withStartValue(0).withEndPoints(0, 1);
        this.openCloseAnimation.reset();
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {

        borderRadius = 5;

        GlStateManager.pushMatrix();

        // Scale Animation
        this.openCloseAnimation.interpolate(this.isClosing, 0.2f);
        RenderSystem.translateAndScale(posX + width * 0.5, posY + height * 0.5, this.openCloseAnimation.value);

        if (this.isClosing && this.openCloseAnimation.value < 0.3) {
            this.isClosing = false;
            mc.displayGuiScreen(null);
        }

//        BloomRenderer.preRenderShadow();
//        RoundedRect.drawRound(posX, posY, width, height, borderRadius, 0.3, Color.WHITE);
//        BloomRenderer.postRenderShadow(1f);
//
//        RoundedRect.drawGradientHorizontal(posX, posY, width, height, borderRadius, new Color(29, 32, 39), new Color(0, 0, 0));

        RoundedRect.drawRound(posX, posY, width, height, borderRadius, new Color(0, 0, 0, 80));
        ShaderUtils.doRoundedBlurAndBloom(posX, posY, width, height, borderRadius);

        this.clientLogo.setPosition(posX + 5, posY + height - this.clientLogo.getHeight() - 5);
        this.clientLogo.setBounds(32, 32);
        this.clientLogo.draw();

        ZFontRenderer shs16 = FontManager.pf16;

        String tag = "ZEPHYR";

        ZFontRenderer tagRenderer = FontManager.gsans40;

        tagRenderer.drawString(tag, posX + 52 - tagRenderer.getStringWidth(tag) * 0.5, posY + 24 - tagRenderer.getHeight() * 0.5, -1);

        // Draw Panels

        double panelsOffsetX = posX + 4;
        double panelOffsetY = posY + 45;
        double panelWidth = 100, panelHeight = 18;
        double panelSpacing = 4;

        int currentPanelIndex = this.panels.indexOf(this.currentPanel);

        double y1Dest = panelOffsetY + currentPanelIndex * (panelHeight + panelSpacing) + 4;
        double y2Dest = panelOffsetY + currentPanelIndex * (panelHeight + panelSpacing) + panelHeight - 4;
        float selectorSpeed = 0.7f;

        if (y2Dest > renderValues.panelSelectorY2) {
            renderValues.panelSelectorY2 = AnimationSystem.interpolate(renderValues.panelSelectorY2, y2Dest, selectorSpeed);

            if (renderValues.panelSelectorY2 >= y2Dest - 0.2) {
                renderValues.panelSelectorY1 = AnimationSystem.interpolate(renderValues.panelSelectorY1, y1Dest, selectorSpeed);
            }
        } else {
            renderValues.panelSelectorY1 = AnimationSystem.interpolate(renderValues.panelSelectorY1, y1Dest, selectorSpeed);


            if (renderValues.panelSelectorY1 <= y1Dest + 0.2) {
                renderValues.panelSelectorY2 = AnimationSystem.interpolate(renderValues.panelSelectorY2, y2Dest, selectorSpeed);

            }
        }


        RoundedRect.drawRound(panelsOffsetX, panelOffsetY + currentPanelIndex * (panelHeight + panelSpacing), panelWidth, panelHeight, 2, new Color(0, 0, 0, 100));

        RoundedRect.drawRound(panelsOffsetX, renderValues.panelSelectorY1, 2, renderValues.panelSelectorY2 - renderValues.panelSelectorY1, 1, new Color(0xff0090ff));

        for (Panel panel : this.panels) {

            Panel.RenderValues panelRenderValues = panel.getRenderValues();

            boolean hovered = RenderSystem.isHovered(mouseX, mouseY, panelsOffsetX, panelOffsetY, panelWidth, panelHeight);

            if (this.currentPanel != panel && hovered && Mouse.isButtonDown(0) && !lmbPressed) {
                lmbPressed = true;
                this.currentPanel = panel;
            }

            if (hovered && this.currentPanel != panel) {

                panelRenderValues.hoveredAlpha = AnimationSystem.interpolate(panelRenderValues.hoveredAlpha * 255, 100, 0.2f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;

            } else {
                panelRenderValues.hoveredAlpha = AnimationSystem.interpolate(panelRenderValues.hoveredAlpha * 255, 0, 0.3f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
            }

            RoundedRect.drawRound(panelsOffsetX, panelOffsetY, panelWidth, panelHeight, 2, new Color(39, 45, 60, (int) (panelRenderValues.hoveredAlpha * 255)));


//            FontManager.icon30.drawString(panel.)
            Image.draw(new ResourceLocation("Zephyr/textures/clickgui/panel/" + panel.getName().toLowerCase() + ".png"), panelsOffsetX + 6, panelOffsetY + 1, 16, 16, Image.Type.Normal);
            shs16.drawString(panel.getName(), panelsOffsetX + 25, panelOffsetY + panelHeight * 0.5 - shs16.getHeight() * 0.5, ColorUtils.getColor(ColorUtils.ColorType.Text));

            panelOffsetY += panelHeight + panelSpacing;
        }

        double panelRenderX = posX + 110, panelRenderY = posY + 5;
        double panelRenderWidth = posX + width - panelRenderX - 5, panelRenderHeight = posY + height - panelRenderY - 5;

        int dWheel = Mouse.getDWheel();

        this.currentPanel.draw(panelRenderX, panelRenderY, panelRenderWidth, panelRenderHeight, mouseX, mouseY, dWheel);


        if (RenderSystem.isHovered(mouseX, mouseY, posX, posY, width, 20) && Mouse.isButtonDown(0)) {
            lmbPressed = true;

            if (moveX == 0 && moveY == 0) {
                moveX = mouseX - posX;
                moveY = mouseY - posY;
            } else {
                renderValues.panelSelectorY1 += mouseY - moveY - posY;
                renderValues.panelSelectorY2 += mouseY - moveY - posY;

                musicPanel.listSelectorY1 += mouseY - moveY - posY;
                musicPanel.listSelectorY2 += mouseY - moveY - posY;

                posX = mouseX - moveX;
                posY = mouseY - moveY;
            }
        } else if (moveX != 0 || moveY != 0) {
            moveX = 0;
            moveY = 0;
        }

        if (RenderSystem.isHovered(mouseX, mouseY, posX + width - 16, posY + height - 16, 24, 24)) {
            if (Mouse.isButtonDown(0) && !dragging) {
                dragging = true;
                this.lmbPressed = true;
            }
        }

        if (!Mouse.isButtonDown(0) && dragging)
            dragging = false;

        if (dragging) {
            if (moveXW == 0 && moveYW == 0) {
                moveXW = mouseX - width;
                moveYW = mouseY - height;
            } else {
                if (mouseX - moveXW >= 640) {
                    width = mouseX - moveXW;
                }

                if (mouseY - moveYW >= 360) {
                    height = mouseY - moveYW;
                }
            }
        } else if (moveXW != 0 || moveYW != 0) {
            moveXW = 0;
            moveYW = 0;
        }

        GlStateManager.popMatrix();

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;

        if (!Mouse.isButtonDown(1) && rmbPressed)
            rmbPressed = false;

        boolean canMove = !(this.currentPanel instanceof MusicPanel) || !((MusicPanel) this.currentPanel).searchBox.isFocused();
        
        if (canMove) {
            double speed = 0.075;
            double lastY = this.posY;
            if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                this.posY = AnimationSystem.interpolate(this.posY, 0, speed);
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                this.posY = AnimationSystem.interpolate(this.posY, RenderSystem.getHeight() - this.height, speed);

            }

            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                this.posX = AnimationSystem.interpolate(this.posX, 0, speed);
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                this.posX = AnimationSystem.interpolate(this.posX, RenderSystem.getWidth() - this.width, speed);
            }

            renderValues.panelSelectorY1 += posY - lastY;
            renderValues.panelSelectorY2 += posY - lastY;

            musicPanel.listSelectorY1 += posY - lastY;
            musicPanel.listSelectorY2 += posY - lastY;
        }

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.currentPanel.keyTyped(typedChar, keyCode))
            return;

        if (keyCode == Keyboard.KEY_ESCAPE)
            this.isClosing = !this.isClosing;

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.currentPanel.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.currentPanel.mouseReleased(mouseX, mouseY, state);
    }

    private class RenderValues {

        public double panelSelectorY1, panelSelectorY2;

    }
}
