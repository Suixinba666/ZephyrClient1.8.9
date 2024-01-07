package tech.imxianyu.module.impl.render.hud;

import net.minecraft.client.Minecraft;
import org.lwjglx.input.Keyboard;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.Bloom;
import tech.imxianyu.rendering.Blur;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.settings.ZephyrSettings;

import java.awt.*;

/**
 * @author ImXianyu
 * @since 7/11/2023 9:26 AM
 */
public class TabGuiRenderer {
    
    Module.Category currentCategory = Module.Category.values()[0];
    public double panelSelectorY1, panelSelectorY2, panelHighLightY;

    ModuleListRenderer moduleListRenderer = new ModuleListRenderer(currentCategory);

    public void draw(double posX, double posY) {
        double panelsOffsetX = posX;
        double panelOffsetY = posY;
        double panelWidth = 80, panelHeight = 16;
        double panelSpacing = 2;

        int currentPanelIndex = Module.Category.getInIndex(currentCategory);

        double y1Dest = currentPanelIndex * (panelHeight + panelSpacing) + 3;
        double y2Dest = currentPanelIndex * (panelHeight + panelSpacing) + panelHeight - 3;
        float selectorSpeed = 0.7f;

        if (y2Dest > panelSelectorY2) {
            panelSelectorY2 = AnimationSystem.interpolate(panelSelectorY2, y2Dest, selectorSpeed);

            if (panelSelectorY2 >= y2Dest - 0.2) {
                panelSelectorY1 = AnimationSystem.interpolate(panelSelectorY1, y1Dest, selectorSpeed);
            }
        } else {
            panelSelectorY1 = AnimationSystem.interpolate(panelSelectorY1, y1Dest, selectorSpeed);


            if (panelSelectorY1 <= y1Dest + 0.2) {
                panelSelectorY2 = AnimationSystem.interpolate(panelSelectorY2, y2Dest, selectorSpeed);

            }
        }

        ZFontRenderer shs16 = FontManager.pf16;

        panelHighLightY = AnimationSystem.interpolate(panelHighLightY, currentPanelIndex * (panelHeight + panelSpacing), 0.4);


        if (!ZephyrSettings.reduceShaders.getValue()) {
            ShaderUtils.doRoundedBlurAndBloom(panelsOffsetX - 4, panelOffsetY - 4, panelWidth + 8, (panelHeight + panelSpacing) * Module.Category.values().length - panelSpacing + 8, 2);

            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

            RoundedRect.drawRound(panelsOffsetX - 4, panelOffsetY - 4, panelWidth + 8, (panelHeight + panelSpacing) * Module.Category.values().length - panelSpacing + 8, 2, new Color(0, 0, 0, 50));
        } else {
            RoundedRect.drawRound(panelsOffsetX - 4, panelOffsetY - 4, panelWidth + 8, (panelHeight + panelSpacing) * Module.Category.values().length - panelSpacing + 8, 2, new Color(ColorUtils.getColor(ColorUtils.ColorType.Base)));
        }


        RoundedRect.drawRound(panelsOffsetX, panelOffsetY + panelHighLightY, panelWidth, panelHeight, 2, new Color(255, 255, 255, 20));

        RoundedRect.drawRound(panelsOffsetX, panelOffsetY +  panelSelectorY1, 2, panelSelectorY2 - panelSelectorY1, 1, new Color(0xff0090ff));

        for (Module.Category panel : Module.Category.values()) {

            FontManager.icon25.drawString(panel.getIcon(), panelsOffsetX + 6, panelOffsetY + panelHeight * 0.5 - FontManager.icon25.getHeight() * 0.5, ColorUtils.getColor(ColorUtils.ColorType.Text));
            shs16.drawString(panel.getName(), panelsOffsetX + 25, panelOffsetY + panelHeight * 0.5 - shs16.getHeight() * 0.5, ColorUtils.getColor(ColorUtils.ColorType.Text));

            panelOffsetY += panelHeight + panelSpacing;
        }

        if (expanded) {
            moduleListRenderer.render(panelsOffsetX + panelWidth + 14, posY);

            if (moduleListRenderer.closing && moduleListRenderer.panelStencilWidth < 0.2) {
                expanded = false;
            }
        }
    }

    public boolean expanded = false;

    public void keyTyped(int keyCode) {

        if (Keyboard.KEY_RIGHT == keyCode) {
            if (!expanded) {
                moduleListRenderer = new ModuleListRenderer(currentCategory);
                expanded = true;
            } else {
                moduleListRenderer.closing = false;
            }
        }

        if (!expanded) {
            if (Keyboard.KEY_DOWN == keyCode)
                next();

            if (Keyboard.KEY_UP == keyCode)
                previous();
        } else {
            moduleListRenderer.keyTyped(keyCode);
        }

        if (Keyboard.KEY_LEFT == keyCode && expanded) {
            moduleListRenderer.closing = true;
        }
    }

    private void next() {
        int next = Module.Category.getInIndex(currentCategory) + 1;
        if (next < Module.Category.values().length) {
            currentCategory = Module.Category.values()[next];
        } else {
            currentCategory = Module.Category.values()[0];
        }
    }

    private void previous() {
        int prev = Module.Category.getInIndex(currentCategory) - 1;
        if (prev >= 0) {
            currentCategory = Module.Category.values()[prev];
        } else {
            currentCategory = Module.Category.values()[Module.Category.values().length - 1];
        }
    }

}
