package tech.imxianyu.gui.clickgui.panel.impl;

import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.imxianyu.gui.clickgui.ZephyrClickGui;
import tech.imxianyu.gui.clickgui.panel.Panel;
import tech.imxianyu.gui.clickgui.panel.impl.widgetspanel.WidgetsSettingsRenderer;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.management.WidgetsManager;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.Stencil;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.BloomRenderer;
import tech.imxianyu.widget.Widget;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ImXianyu
 * @since 5/2/2023 7:03 PM
 */
public class WidgetsPanel extends Panel {

    Map<Widget, WidgetRenderValues> renderValuesMap = new HashMap<>();
    WidgetsSettingsRenderer settingsRenderer = null;
    double yScroll = 0, ySmooth = 0;

    public WidgetsPanel() {
        super("Widgets");
    }

    @Override
    public void init() {

    }

    @Override
    public void draw(double posX, double posY, double width, double height, double mouseX, double mouseY, int dWheel) {
//        FontManager.gsans40.drawString("LOL", posX, posY, -1);

        ZFontRenderer unicodeRenderer = FontManager.pf40;

        unicodeRenderer.drawString("Widgets", posX, posY, -1);

        ZFontRenderer fontRenderer = FontManager.pf18;
        ZFontRenderer smallerFontRenderer = FontManager.pf16;

        int numberOfLines = 2;
        double verticalSpacing = 8, horizontalSpacing = 5;

        double yAdd = 5;
        if (RenderSystem.isHovered(mouseX, mouseY, posX, posY + unicodeRenderer.getHeight() + 4, width, height - (unicodeRenderer.getHeight() + 4)) && dWheel != 0 && settingsRenderer == null) {
            if (dWheel > 0)
                ySmooth -= yAdd;
            else
                ySmooth += yAdd;
        }

        ySmooth = AnimationSystem.interpolate(ySmooth, 0, 0.2f);
        yScroll = AnimationSystem.interpolate(yScroll, yScroll + ySmooth, 0.6f);

        if (yScroll < 0)
            yScroll = AnimationSystem.interpolate(yScroll, 0, 0.2f);

        int count = 0;
        double offsetX = posX + 3, offsetY = posY + unicodeRenderer.getHeight() + 12 - yScroll;
        double widgetWidth = /*100*/(width - horizontalSpacing * 3) / numberOfLines, widgetHeight = 30;
        Stencil.write();
        Rect.draw(posX, posY + unicodeRenderer.getHeight() + 4, width, height - (unicodeRenderer.getHeight() + 4), -1, Rect.RectType.EXPAND);
        Stencil.erase(true);

//        BloomRenderer.preRenderShadow();
//        offsetX = posX + 3;
//
//        List<Widget> w = WidgetsManager.getWidgets();
//
//        count = 0;
//        for (int i = 0; i < w.size(); i++) {
//            if ((count) % numberOfLines == 0) {
//                offsetX = posX + 3;
//
//            } else {
//                offsetX += widgetWidth + horizontalSpacing;
//            }
//
//            RoundedRect.drawRound(offsetX, offsetY, widgetWidth, widgetHeight, 1, new Color(41, 43, 51));
//
//            if (count != 0 && (count - 1) % numberOfLines == 0) {
//                offsetY += widgetHeight + verticalSpacing;
//            }
//
//            count++;
//        }
//        offsetY += widgetHeight + verticalSpacing;
//
//        BloomRenderer.postRenderShadow();

        offsetX = posX + 3;
        offsetY = posY + unicodeRenderer.getHeight() + 12 - yScroll;
        widgetWidth = /*100*/(width - horizontalSpacing * 3) / numberOfLines;
        widgetHeight = 30;


        offsetX = posX + 3;

        List<Widget> widgets = WidgetsManager.getWidgets();

        count = 0;

        for (Widget widget : widgets) {

            if (!widget.getShouldRender().get())
                continue;

            WidgetRenderValues renderValue = this.getRenderValue(widget);


            if ((count) % numberOfLines == 0) {
//                    count = 0;
                offsetX = posX + 3;

            } else {
                offsetX += widgetWidth + horizontalSpacing;
            }

            RoundedRect.drawRound(offsetX, offsetY, widgetWidth, widgetHeight, 1, new Color(0, 0, 0, 120));

            if (renderValue.hoveredAlpha != 0) {
                RoundedRect.drawRound(offsetX, offsetY, widgetWidth, widgetHeight, 1, new Color(1, 1, 1, renderValue.hoveredAlpha));

            }

            double stringsX = offsetX + 34;
            fontRenderer.drawString(widget.getName(), stringsX, offsetY + 3, -1);
            smallerFontRenderer.drawString(widget.getDescription(), stringsX, offsetY + 5 + fontRenderer.getHeight(), Color.GRAY.getRGB());

            double switchX = offsetX + widgetWidth - 25, switchY = offsetY + 11, switchWidth = 20, switchHeight = 9, switchShrink = 0.75, switchRadius = 3.5;
            double circleSize = 6;
            RoundedRect.drawRound(switchX, switchY, switchWidth, switchHeight, switchRadius, widget.isEnabled() ? new Color(76, 194, 255) : new Color(155, 158, 164));

            if (!widget.isEnabled())
                RoundedRect.drawRound(switchX + switchShrink, switchY + switchShrink, switchWidth - switchShrink * 2, switchHeight - switchShrink * 2, switchRadius - 1, new Color(41, 43, 51));

            RoundedRect.drawRound(switchX + switchShrink + switchShrink + renderValue.enabledSwitch, switchY + switchShrink + switchShrink, circleSize, circleSize, 2, widget.isEnabled() ? Color.BLACK : new Color(155, 158, 164));

            renderValue.enabledSwitch = AnimationSystem.interpolate(renderValue.enabledSwitch, widget.isEnabled() ? 11 : 0, 0.2);

                /*if (widget.isEnabled()) {
                    Rect.draw(offsetX + widgetWidth - 12, offsetY + 12, 6, 6, RenderSystem.hexColor(155, 158, 164), Rect.RectType.EXPAND);
                } else {
                    Rect.draw(offsetX + widgetWidth - 24, offsetY + 12, 6, 6, RenderSystem.hexColor(155, 158, 164), Rect.RectType.EXPAND);
                }*/

            if (RenderSystem.isHovered(mouseX, mouseY, offsetX, offsetY, widgetWidth, widgetHeight) && this.settingsRenderer == null) {

                if (Mouse.isButtonDown(1) && !ZephyrClickGui.getInstance().rmbPressed) {
                    ZephyrClickGui.getInstance().rmbPressed = true;

                    this.settingsRenderer = new WidgetsSettingsRenderer(widget);
                }

                if (Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                    ZephyrClickGui.getInstance().lmbPressed = true;

                    widget.toggle();
                }

                renderValue.hoveredAlpha = AnimationSystem.interpolate(renderValue.hoveredAlpha * 255, 50, 0.2f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
            } else {
                renderValue.hoveredAlpha = AnimationSystem.interpolate(renderValue.hoveredAlpha * 255, 0, 0.2f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
            }


            if (count != 0 && (count - 1) % numberOfLines == 0) {
                offsetY += widgetHeight + verticalSpacing;
            }

            count++;
        }

        if (settingsRenderer != null) {
            if (settingsRenderer.canClose())
                settingsRenderer = null;
            else
                settingsRenderer.render(mouseX, mouseY, posX, posY + unicodeRenderer.getHeight() + 4, width, height - (unicodeRenderer.getHeight() + 4), dWheel);
        }

        Stencil.dispose();
    }

    private WidgetRenderValues getRenderValue(Widget widget) {

        if (!this.renderValuesMap.containsKey(widget))
            this.renderValuesMap.put(widget, new WidgetRenderValues());

        return this.renderValuesMap.get(widget);
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE && this.settingsRenderer != null) {
            this.settingsRenderer.close();
            return true;
        }

        return false;
    }

    private class WidgetRenderValues {
        public float hoveredAlpha = 0;
        public double enabledSwitch = 0;
    }
}
