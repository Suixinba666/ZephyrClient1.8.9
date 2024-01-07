package tech.imxianyu.module.impl.render.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjglx.input.Keyboard;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.*;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.StencilShader;
import tech.imxianyu.settings.ZephyrSettings;

import java.awt.*;
import java.util.List;

/**
 * @author ImXianyu
 * @since 7/11/2023 9:47 AM
 */
public class ModuleListRenderer {
    public Module.Category category;
    public List<Module> modules;
    Module currentModule;
    public double panelSelectorY1, panelSelectorY2, panelHighLightY, panelStencilWidth = 0;

    public boolean closing = false;

    Framebuffer mixBuffer, contentBuffer;

    public ModuleListRenderer(Module.Category category) {
        this.category = category;
        this.modules = ModuleManager.getModulesByCategory(category);
        this.currentModule = this.modules.get(0);
    }

    public void render(double posX, double posY) {
        double panelsOffsetX = posX;
        double panelOffsetY = posY;
        double panelWidth = 90, panelHeight = 16;
        double panelSpacing = 2;

        int currentPanelIndex = modules.indexOf(currentModule);

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

        panelStencilWidth = AnimationSystem.interpolate(panelStencilWidth, closing ? 0 : (panelWidth + 12), 0.4);
        panelHighLightY = AnimationSystem.interpolate(panelHighLightY, currentPanelIndex * (panelHeight + panelSpacing), 0.4);


        if (!ZephyrSettings.reduceShaders.getValue()) {
            mixBuffer = RenderSystem.createFrameBuffer(mixBuffer);
            contentBuffer = RenderSystem.createFrameBuffer(contentBuffer);
            mixBuffer.framebufferClear();
            contentBuffer.framebufferClear();

            mixBuffer.bindFramebuffer(true);
            Rect.draw(panelsOffsetX - 6, panelOffsetY - 6, panelStencilWidth, (panelHeight + panelSpacing) * modules.size() - panelSpacing + 12, -1, Rect.RectType.EXPAND);
            mixBuffer.unbindFramebuffer();
            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

            contentBuffer.bindFramebuffer(true);

            ShaderUtils.doRoundedBlurAndBloom(panelsOffsetX - 4, panelOffsetY - 4, panelStencilWidth - 4, (panelHeight + panelSpacing) * modules.size() - panelSpacing + 8, 2);

            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
            RoundedRect.drawRound(panelsOffsetX - 4, panelOffsetY - 4, panelStencilWidth - 4, (panelHeight + panelSpacing) * modules.size() - panelSpacing + 8, 2, new Color(0, 0, 0, 50));

            contentBuffer.bindFramebuffer(true);

        } else {
            Stencil.write();
            Rect.draw(panelsOffsetX - 6, panelOffsetY - 6, panelStencilWidth, (panelHeight + panelSpacing) * modules.size() - panelSpacing + 12, -1, Rect.RectType.EXPAND);
            Stencil.erase(true);

            GlStateManager.disableAlpha();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            RoundedRect.drawRound(panelsOffsetX - 4, panelOffsetY - 4, panelWidth + 8, (panelHeight + panelSpacing) * modules.size() - panelSpacing + 8, 2, new Color(ColorUtils.getColor(ColorUtils.ColorType.Base)));

        }

//        RoundedRect.drawRound(panelsOffsetX, panelOffsetY + panelHighLightY, panelWidth, panelHeight, 2, new Color(255, 255, 255, ZephyrSettings.reduceShaders.getValue() ? 20 : 80));

        RoundedRect.drawRound(panelsOffsetX, panelOffsetY +  panelSelectorY1, 2, panelSelectorY2 - panelSelectorY1, 1, new Color(0xff0090ff));

        for (Module module : this.modules) {

            shs16.drawString(module.getName(), panelsOffsetX + 6, panelOffsetY + panelHeight * 0.5 - shs16.getHeight() * 0.5, module.isEnabled() ? RenderSystem.hexColor(255, 255, 255) : RenderSystem.hexColor(255, 255, 255, 120));

            panelOffsetY += panelHeight + panelSpacing;
        }

        if (!ZephyrSettings.reduceShaders.getValue()) {
            contentBuffer.unbindFramebuffer();
            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

            StencilShader.render(contentBuffer.framebufferTexture, mixBuffer.framebufferTexture);
        } else {
            Stencil.dispose();
        }
    }

    private void next() {
        int next = this.modules.indexOf(this.currentModule) + 1;
        if (next < this.modules.size()) {
            currentModule = this.modules.get(next);
        } else {
            currentModule = this.modules.get(0);
        }
    }

    private void previous() {
        int prev = this.modules.indexOf(this.currentModule) - 1;
        if (prev >= 0) {
            currentModule = this.modules.get(prev);
        } else {
            currentModule = this.modules.get(this.modules.size() - 1);
        }
    }

    public boolean keyTyped(int keyCode) {

        if (closing || panelStencilWidth < 89)
            return false;

        if (Keyboard.KEY_DOWN == keyCode)
            next();

        if (Keyboard.KEY_UP == keyCode)
            previous();

        if (Keyboard.KEY_RETURN == keyCode || Keyboard.KEY_RIGHT == keyCode)
            currentModule.toggle();

        return false;
    }

}
