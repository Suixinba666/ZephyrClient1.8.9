package tech.imxianyu.gui.clickgui.panel.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.imxianyu.gui.clickgui.ZephyrClickGui;
import tech.imxianyu.gui.clickgui.panel.Panel;
import tech.imxianyu.gui.clickgui.panel.impl.modulespanel.ModuleSettingsRenderer;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.Stencil;
import tech.imxianyu.rendering.TextField;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.BloomRenderer;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author ImXianyu
 * @since 5/2/2023 7:03 PM
 */
public class ModulesPanel extends Panel {

    Map<Module, ModuleRenderValues> renderValuesMap = new HashMap<>();
    ModuleSettingsRenderer settingsRenderer = null;
    double yScroll = 0, ySmooth = 0;

    public TextField searchBox = new TextField(0, 0, 0, 0, 0);

    public ModulesPanel() {
        super("Modules");
    }

    @Override
    public void init() {
        searchBox.setPlaceholder("Search (Ctrl + F)");
    }

    @Override
    public void draw(double posX, double posY, double width, double height, double mouseX, double mouseY, int dWheel) {
//        FontManager.gsans40.drawString("LOL", posX, posY, -1);

        GlStateManager.alphaFunc(516, 0.0F);

        ZFontRenderer unicodeRenderer = FontManager.pf40;

        unicodeRenderer.drawString("Modules", posX, posY, -1);

        searchBox.xPosition = (float) (posX + 12 + unicodeRenderer.getStringWidth("Modules"));
        searchBox.yPosition = (float) (posY + 10);
        if (searchBox.isFocused())
            searchBox.onTick();
        searchBox.width = 100;
        searchBox.height = 10;
        searchBox.setTextColor(Color.WHITE.getRGB());
        searchBox.setDisabledTextColour(Color.GRAY.getRGB());
        searchBox.drawTextBox((int) mouseX, (int) mouseY);

        ZFontRenderer fontRenderer = FontManager.pf18;
        ZFontRenderer smallerFontRenderer = FontManager.pf16;

        int numberOfLines = 2;
        double verticalSpacing = 3, horizontalSpacing = 5;

        double yAdd = 5;
        if (RenderSystem.isHovered(mouseX, mouseY, posX, posY + unicodeRenderer.getHeight() + 4, width, height - (unicodeRenderer.getHeight() + 4)) && dWheel != 0 && settingsRenderer == null) {
            if (dWheel > 0)
                ySmooth -= yAdd;
            else
                ySmooth += yAdd;
        }

        ySmooth = AnimationSystem.interpolate(ySmooth, 0, 0.1f);
        yScroll = AnimationSystem.interpolate(yScroll, yScroll + ySmooth, 0.6f);

        if (yScroll < 0)
            yScroll = AnimationSystem.interpolate(yScroll, 0, 0.2f);

        int count = 0;
        double offsetX, offsetY;
        double moduleWidth, moduleHeight;
        Stencil.write();
        Rect.draw(posX, posY + unicodeRenderer.getHeight() + 4, width, height - (unicodeRenderer.getHeight() + 4), -1, Rect.RectType.EXPAND);
        Stencil.erase(true);

        offsetX = posX + 3;
        offsetY = posY + unicodeRenderer.getHeight() + 4 - yScroll;
        moduleWidth = /*100*/(width - horizontalSpacing * 3) / numberOfLines;
        moduleHeight = 30;

        List<Module.Category> categories = new ArrayList<>(List.of(Module.Category.values()));

        Iterator<Module.Category> it = categories.iterator();

        while (it.hasNext()) {
            Module.Category cat = it.next();

            int ct = 0;
            List<Module> modules = ModuleManager.getModulesByCategory(cat);

            for (Module module : modules) {
                if (!module.getShouldRender().get() || !this.shouldShowModule(module))
                    continue;
                ct ++;
            }

            if (ct == 0)
                it.remove();
        }


        for (Module.Category category : categories) {

            offsetX = posX + 3;

            fontRenderer.drawString(category.getName(), offsetX, offsetY + 5, -1);

            offsetY += 10 + fontRenderer.getHeight();

            List<Module> modules = ModuleManager.getModulesByCategory(category);

            count = 0;

            for (Module module : modules) {

                if (!module.getShouldRender().get() || !this.shouldShowModule(module))
                    continue;

                ModuleRenderValues renderValue = this.getRenderValue(module);

                if ((count) % numberOfLines == 0) {
//                    count = 0;
                    offsetX = posX + 3;

                } else {
                    offsetX += moduleWidth + horizontalSpacing;

                }

                RoundedRect.drawRound(offsetX, offsetY, moduleWidth, moduleHeight, 1, new Color(0, 0, 0, 120));

                if (renderValue.hoveredAlpha != 0) {
                    RoundedRect.drawRound(offsetX, offsetY, moduleWidth, moduleHeight, 1, new Color(1, 1, 1, renderValue.hoveredAlpha));
                }

//                GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD);
//                GL11.glColor4f(1f, 1f, 1f, 1f);
//                Image.draw(this.getModuleIcon(module), offsetX + 4, offsetY + 3, 24, 24, Image.Type.NoColor);

                double stringsX = offsetX + 34;
                fontRenderer.drawString(module.getName(), stringsX, offsetY + 3, -1);
                smallerFontRenderer.drawString(module.getDescription(), stringsX, offsetY + 5 + fontRenderer.getHeight(), Color.GRAY.getRGB());

                double switchX = offsetX + moduleWidth - 25, switchY = offsetY + 11, switchWidth = 20, switchHeight = 9, switchShrink = 0.75, switchRadius = 3.5;
                double circleSize = 6;
                RoundedRect.drawRound(switchX, switchY, switchWidth, switchHeight, switchRadius, module.isEnabled() ? new Color(76, 194, 255) : new Color(155, 158, 164));

                if (!module.isEnabled())
                    RoundedRect.drawRound(switchX + switchShrink, switchY + switchShrink, switchWidth - switchShrink * 2, switchHeight - switchShrink * 2, switchRadius - 1, new Color(41, 43, 51));

                RoundedRect.drawRound(switchX + switchShrink + switchShrink + renderValue.enabledSwitch, switchY + switchShrink + switchShrink, circleSize, circleSize, 2, module.isEnabled() ? Color.BLACK : new Color(155, 158, 164));

                renderValue.enabledSwitch = AnimationSystem.interpolate(renderValue.enabledSwitch, module.isEnabled() ? 11 : 0, 0.2);

                /*if (module.isEnabled()) {
                    Rect.draw(offsetX + moduleWidth - 12, offsetY + 12, 6, 6, RenderSystem.hexColor(155, 158, 164), Rect.RectType.EXPAND);
                } else {
                    Rect.draw(offsetX + moduleWidth - 24, offsetY + 12, 6, 6, RenderSystem.hexColor(155, 158, 164), Rect.RectType.EXPAND);
                }*/

                if (RenderSystem.isHovered(mouseX, mouseY, offsetX, offsetY, moduleWidth, moduleHeight) && this.settingsRenderer == null) {

                    if (Mouse.isButtonDown(1) && !ZephyrClickGui.getInstance().rmbPressed) {
                        ZephyrClickGui.getInstance().rmbPressed = true;

                        this.settingsRenderer = new ModuleSettingsRenderer(module);
                    }

                    if (Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                        ZephyrClickGui.getInstance().lmbPressed = true;

                        module.toggle();
                    }

                    renderValue.hoveredAlpha = AnimationSystem.interpolate(renderValue.hoveredAlpha * 255, 50, 0.2f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
                } else {
                    renderValue.hoveredAlpha = AnimationSystem.interpolate(renderValue.hoveredAlpha * 255, 0, 0.2f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
                }


                if (count != 0 && (count - 1) % numberOfLines == 0) {
                    offsetY += moduleHeight + verticalSpacing;
                }

                count++;
            }

            offsetY += moduleHeight + verticalSpacing;
        }

        if (settingsRenderer != null) {
            if (settingsRenderer.canClose())
                settingsRenderer = null;
            else
                settingsRenderer.render(mouseX, mouseY, posX, posY + unicodeRenderer.getHeight() + 4, width, height - (unicodeRenderer.getHeight() + 4), dWheel);
        }

        Stencil.dispose();
    }

    private boolean shouldShowModule(Module module) {

        if (this.searchBox.getText().isEmpty())
            return true;

        String text = this.searchBox.getText();

        String[] s = text.toLowerCase().split(" ");

        String lowerCase = module.getName().toLowerCase();

        for (String split : s) {
            if (!lowerCase.contains(split))
                return false;
        }

        return true;
    }

    private ResourceLocation getModuleIcon(Module module) {
        return new ResourceLocation("Zephyr/textures/modules/" + module.getCategory().getName().toLowerCase() + "/" + module.getName().toLowerCase().replaceAll(" ", "") + ".png");
    }

    private ModuleRenderValues getRenderValue(Module module) {

        if (!this.renderValuesMap.containsKey(module))
            this.renderValuesMap.put(module, new ModuleRenderValues());

        return this.renderValuesMap.get(module);
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE && this.settingsRenderer != null) {
            this.settingsRenderer.close();
            return true;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (this.searchBox.isFocused()) {
                this.searchBox.setFocused(false);
                return true;
            }
        }

        if (searchBox.isFocused()) {
            this.searchBox.textboxKeyTyped(typedChar, keyCode);
            return true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                && keyCode == Keyboard.KEY_F
                && !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
                && !(Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))) {

            this.searchBox.setFocused(true);
            return true;
        }

        return false;
    }

    @Override
    public void mouseClicked(int mX, int mY, int mouseButton) {
        double mouseX = mX * RenderSystem.getScaleFactor();
        double mouseY = mY * RenderSystem.getScaleFactor();
        this.searchBox.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mX, int mY, int state) {
        double mouseX = mX * RenderSystem.getScaleFactor();
        double mouseY = mY * RenderSystem.getScaleFactor();
        this.searchBox.mouseReleased(mouseX, mouseY, state);
    }

    private class ModuleRenderValues {
        public float hoveredAlpha = 0;
        public double enabledSwitch = 0;
    }
}
