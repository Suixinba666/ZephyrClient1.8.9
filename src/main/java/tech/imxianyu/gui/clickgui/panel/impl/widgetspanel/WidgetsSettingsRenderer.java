package tech.imxianyu.gui.clickgui.panel.impl.widgetspanel;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.input.Mouse;
import tech.imxianyu.gui.clickgui.ZephyrClickGui;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.Stencil;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.BloomRenderer;
import tech.imxianyu.settings.*;
import tech.imxianyu.widget.Widget;

import java.awt.*;
import java.util.List;

/**
 * @author ImXianyu
 * @since 6/4/2023 5:21 PM
 */
public class WidgetsSettingsRenderer {

    @Getter
    private final Widget widget;
    @Getter
    public boolean closing = false;
    float screeMaskAlpha = 0;
    double scale = 0;
    double yScroll = 0, yScrollSmooth = 0;

    public WidgetsSettingsRenderer(Widget widget) {
        this.widget = widget;
    }

    public void render(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {
        screeMaskAlpha = AnimationSystem.interpolate(screeMaskAlpha * 255, this.isClosing() ? 0 : 120, 0.3f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
        scale = AnimationSystem.interpolate(scale, this.isClosing() ? 0 : 0.99999, 0.2);

        ZFontRenderer fr25 = FontManager.pf25;
        ZFontRenderer fr18 = FontManager.pf18;
        ZFontRenderer fr16 = FontManager.pf16;

        Rect.draw(posX, posY, width, height, RenderSystem.hexColor(0, 0, 0, (int) (screeMaskAlpha * 255)), Rect.RectType.EXPAND);

        GlStateManager.pushMatrix();
        RenderSystem.translateAndScale(posX + width / 2.0, posY + height / 2.0, scale);

        double pWidth = width / 2.0;
        double pHeight = height / 1.2;

        BloomRenderer.preRenderShadow();

        double x = posX + width / 2.0 - pWidth / 2.0;
        double y = posY + height / 2.0 - pHeight / 2.0;

        double yAddAdd = 5;
        if (RenderSystem.isHovered(mouseX, mouseY, x, y, pWidth, pHeight) && dWheel != 0) {
            if (dWheel > 0)
                yScrollSmooth -= yAddAdd;
            else
                yScrollSmooth += yAddAdd;
        }

        yScrollSmooth = AnimationSystem.interpolate(yScrollSmooth, 0, 0.2f);
        yScroll += yScrollSmooth;

        if (yScroll < 0)
            yScroll = AnimationSystem.interpolate(yScroll, 0, 0.2f);

        double hSpacing = 8, vSpacing = 8;

        RoundedRect.drawRound(x, y, pWidth, pHeight, 5, new Color(43, 43, 43));

        BloomRenderer.postRenderShadow();

//        RenderSystem.doScissor((inta) x, (int) y, (int) pWidth, (int) pHeight);

        RoundedRect.drawRound(x, y, pWidth, pHeight, 5, new Color(43, 43, 43));

        if (RenderSystem.isHovered(mouseX, mouseY, posX, posY, width, height) && !RenderSystem.isHovered(mouseX, mouseY, x, y, pWidth, pHeight, -4) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
            ZephyrClickGui.getInstance().lmbPressed = true;
            this.close();
        }


        x += hSpacing;
        y += vSpacing;

        fr25.drawString(widget.getName(), x, y, -1);
//        Rect.draw(x - 8, y + fr25.getHeight(), pWidth, pHeight - 24, -1, Rect.RectType.EXPAND);


        Stencil.write();
        Rect.draw(x - 8, y + fr25.getHeight(), pWidth, pHeight - 24, -1, Rect.RectType.EXPAND);
        Stencil.erase(true);

        y += fr25.getHeight() + vSpacing * 0.5 - yScroll;

        List<Setting<?>> settings = widget.getSettings();

        if (settings.isEmpty()) {
            fr16.drawString("No Settings Here.", x, y, Color.GRAY.getRGB());
            GlStateManager.popMatrix();
            return;
        }

        for (Setting<?> s : settings) {

            if (y > posY + height / 2.0 + pHeight / 2.0)
                break;

            double settingHeight = this.getSettingHeight(s);

            if (!s.shouldRender())
                continue;

            if (y + settingHeight < posY + height / 2.0 - pHeight / 2.0) {
                y += settingHeight;
                continue;
            }

            if (s instanceof BooleanSetting) {
                BooleanSetting setting = (BooleanSetting) s;
                fr16.drawString(s.getName(), x, y, -1);

                double switchWidth = 20, switchHeight = 9, switchX = x + pWidth - 15 - switchWidth, switchY = y, switchShrink = 0.75, switchRadius = 3.5;
                double circleSize = 6;
                RoundedRect.drawRound(switchX, switchY, switchWidth, switchHeight, switchRadius, setting.getValue() ? new Color(76, 194, 255) : new Color(155, 158, 164));

                if (!setting.getValue())
                    RoundedRect.drawRound(switchX + switchShrink, switchY + switchShrink, switchWidth - switchShrink * 2, switchHeight - switchShrink * 2, switchRadius - 1, new Color(41, 43, 51));

                RoundedRect.drawRound(switchX + switchShrink + switchShrink + setting.switchEnabled, switchY + switchShrink + switchShrink, circleSize, circleSize, 2, setting.getValue() ? Color.BLACK : new Color(155, 158, 164));

                setting.switchEnabled = AnimationSystem.interpolate(setting.switchEnabled, setting.getValue() ? 11 : 0, 0.2);

                if (RenderSystem.isHovered(mouseX, mouseY, switchX, switchY, switchWidth, switchHeight) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                    ZephyrClickGui.getInstance().lmbPressed = true;
                    setting.toggle();
                }

            } else if (s instanceof NumberSetting) {
                NumberSetting setting = (NumberSetting) s;

                fr16.drawString(s.getName(), x, y, -1);

                double sliderWidth = 90, sliderHeight = 3, sliderX = x + pWidth - 15 - sliderWidth, sliderY = y + fr16.getHeight() * 0.35, sliderRadius = 1;
                RoundedRect.drawRound(sliderX, sliderY, sliderWidth, sliderHeight, sliderRadius, new Color(159, 159, 159));

                setting.nowWidth = AnimationSystem.interpolate(setting.nowWidth, sliderWidth * (setting.getValue().doubleValue() - setting.getMinimum().doubleValue()) / (setting.getMaximum().doubleValue() - setting.getMinimum().doubleValue()), 0.2);
                RoundedRect.drawRound(sliderX, sliderY, setting.nowWidth, sliderHeight, sliderRadius, new Color(76, 194, 255));
                double circleSize = 9, smallCircleSize = 6;

                RoundedRect.drawRound(sliderX + setting.nowWidth - circleSize * 0.5, sliderY + sliderHeight * 0.5 - circleSize * 0.5, circleSize, circleSize, 3, new Color(69, 69, 69));
                RoundedRect.drawRound(sliderX + setting.nowWidth - smallCircleSize * 0.5, sliderY + sliderHeight * 0.5 - smallCircleSize * 0.5, smallCircleSize, smallCircleSize, 2, new Color(76, 194, 255));

                if (RenderSystem.isHovered(mouseX, mouseY, sliderX, y, sliderWidth, fr16.getHeight()) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {

                    /*double mouseXToLeft = mouseX - sliderX;
                    double percent = mouseXToLeft / sliderWidth;

                    double min = setting.getMinimum().doubleValue();
                    double max = setting.getMaximum().doubleValue();

                    double result = max * percent;
                    if (result < min)
                        result = min;

                    if (result > max)
                        result = max;*/
                    double render = setting.getMinimum().doubleValue();
                    double max = setting.getMaximum().doubleValue();
                    double inc = setting.getIncrement().doubleValue();
                    double valAbs = mouseX - sliderX;
                    double perc = valAbs / sliderWidth;
                    perc = Math.min(Math.max(0.0D, perc), 1.0D);
                    double valRel = (max - render) * perc;
                    double val = render + valRel;
                    val = (double) Math.round(val * (1.0D / inc)) / (1.0D / inc);

                    setting.setValue(val);
                }

                fr16.drawString(setting.getStringForRender(), sliderX - 6 - fr16.getStringWidth(setting.getStringForRender()), y, Color.GRAY.getRGB());

            } else if (s instanceof ModeSetting) {
                ModeSetting setting = (ModeSetting) s;

                fr16.drawString(s.getName(), x, y + 4, -1);

                double cbWidth = 90, cbHeight = fr16.getHeight() + 8, cbX = x + pWidth - 15 - cbWidth, cbY = y;
                RoundedRect.drawRound(cbX, cbY, cbWidth, setting.expandedHeight, 3, new Color(55, 55, 55));
                fr16.drawString(setting.getCurMode(), cbX + 4, cbY + cbHeight * 0.5 - fr16.getHeight() * 0.5, -1);

                double totalHeight = setting.expanded ? cbHeight * (setting.getConstants().length + 1) : cbHeight;
                setting.expandedHeight = AnimationSystem.interpolate(setting.expandedHeight, totalHeight, 0.2);

                if (RenderSystem.isHovered(mouseX, mouseY, cbX, cbY, cbWidth, cbHeight) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                    ZephyrClickGui.getInstance().lmbPressed = true;

                    setting.expanded = !setting.expanded;
                }

                if (!RenderSystem.isHovered(mouseX, mouseY, cbX, cbY, cbWidth, totalHeight) && Mouse.isButtonDown(0) && setting.expanded) {
                    setting.expanded = false;
                }

                RenderSystem.doScissor(((int) cbX) - 1, ((int) cbY) - 1, ((int) cbWidth) + 2, ((int) setting.expandedHeight) + 2);

                if (setting.expanded || setting.expandedHeight > cbHeight + 2) {

                    RenderSystem.drawHorizontalLine(cbX + 4, cbY + cbHeight, cbX + cbWidth - 8, cbY + cbHeight, 1, Color.GRAY.getRGB());

                    double startX = cbX + 4, startY = cbY + cbHeight;
                    for (Enum<?> constant : setting.getConstants()) {
                        fr16.drawString(constant.name(), startX, startY + cbHeight * 0.5 - fr16.getHeight() * 0.5, -1);

                        if (RenderSystem.isHovered(mouseX, mouseY, startX, startY, cbWidth - 8, cbHeight) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                            ZephyrClickGui.getInstance().lmbPressed = true;

                            setting.setValue(constant);
                            setting.expanded = false;
                        }

                        startY += cbHeight;
                    }
                }

                RenderSystem.endScissor();

            } else if (s instanceof StringModeSetting) {
                StringModeSetting setting = (StringModeSetting) s;

                fr16.drawString(s.getName(), x, y + 4, -1);

                double cbWidth = 90, cbHeight = fr16.getHeight() + 8, cbX = x + pWidth - 15 - cbWidth, cbY = y;
                RoundedRect.drawRound(cbX, cbY, cbWidth, setting.expandedHeight, 3, new Color(55, 55, 55));
                fr16.drawString(setting.getValue(), cbX + 4, cbY + cbHeight * 0.5 - fr16.getHeight() * 0.5, -1);

                double totalHeight = setting.expanded ? cbHeight * (setting.getModes().size() + 1) : cbHeight;
                setting.expandedHeight = AnimationSystem.interpolate(setting.expandedHeight, totalHeight, 0.2);

                if (RenderSystem.isHovered(mouseX, mouseY, cbX, cbY, cbWidth, cbHeight) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                    ZephyrClickGui.getInstance().lmbPressed = true;

                    setting.expanded = !setting.expanded;
                }

                if (!RenderSystem.isHovered(mouseX, mouseY, cbX, cbY, cbWidth, totalHeight) && Mouse.isButtonDown(0) && setting.expanded) {
                    setting.expanded = false;
                }

                RenderSystem.doScissor(((int) cbX) - 1, ((int) cbY) - 1, ((int) cbWidth) + 2, ((int) setting.expandedHeight) + 2);

                if (setting.expanded || setting.expandedHeight > cbHeight + 2) {

                    RenderSystem.drawHorizontalLine(cbX + 4, cbY + cbHeight, cbX + cbWidth - 8, cbY + cbHeight, 1, Color.GRAY.getRGB());

                    double startX = cbX + 4, startY = cbY + cbHeight;
                    for (String constant : setting.getModes()) {
                        fr16.drawString(constant, startX, startY + cbHeight * 0.5 - fr16.getHeight() * 0.5, -1);

                        if (RenderSystem.isHovered(mouseX, mouseY, startX, startY, cbWidth - 8, cbHeight) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                            ZephyrClickGui.getInstance().lmbPressed = true;

                            setting.setValue(constant);
                            setting.expanded = false;
                        }

                        startY += cbHeight;
                    }
                }

                RenderSystem.endScissor();

            } else if (s instanceof ColorSetting) {
                ColorSetting setting = (ColorSetting) s;
//                Rect.draw(posX + 4, posY, 100 - xSpacing * 2, 75, RenderUtil.hexColor(48, 46, 48, iAlpha), RectType.EXPAND).draw();

                FontManager.pf18.drawString(setting.getName(), x, y, -1);

                setting.setRenderAlpha(255);
                setting.draw((float) mouseX, (float) (mouseY), x + pWidth - 15 - 80, y);
            } else {
                fr16.drawString(s.getName(), x, y, -1);


            }

            y += settingHeight;
        }

        Stencil.dispose();

        GlStateManager.popMatrix();
    }

    private double getSettingHeight(Setting<?> setting) {
        if (setting instanceof ModeSetting)
            return ((ModeSetting<?>) setting).expandedHeight + 8;

        if (setting instanceof StringModeSetting)
            return ((StringModeSetting) setting).expandedHeight + 8;

        if (setting instanceof ColorSetting)
            return 88;

        return FontManager.pf16.getHeight() + 8;
    }

    public void close() {
        this.closing = true;
    }

    public boolean canClose() {
        return this.isClosing() && this.screeMaskAlpha <= 0.05;
    }

}
