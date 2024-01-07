package tech.imxianyu.widget.impl;

import net.minecraft.util.Formatting;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.HSBColor;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ColorSetting;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.widget.Widget;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ImXianyu
 * @since 6/17/2023 10:04 AM
 */
public class ArrayList extends Widget {

    public BooleanSetting showRenderModules = new BooleanSetting("Show Render Category Modules", true);
    public BooleanSetting arrayListShadow = new BooleanSetting("ArrayListShadow", false);

    public ColorSetting arrayListColor = new ColorSetting("ArrayListColor", new HSBColor(255, 255, 255, 255));
    public BooleanSetting arrayListRect = new BooleanSetting("ArrayListRect", false);
    public ColorSetting arrayListRectColor = new ColorSetting("ArrayListRectColor", new HSBColor(0, 0, 0, 200), () -> arrayListRect.getValue());
    public BooleanSetting arrayListOutline = new BooleanSetting("ArrayListOutline", false);
    public ColorSetting arrayListOutlineColor = new ColorSetting("ArrayListOutlineColor", new HSBColor(255, 255, 255, 200), () -> arrayListOutline.getValue());
    Map<Module, ModuleNamePosition> renderMap = new HashMap<>();


    public ArrayList() {
        super("ArrayList");
        super.setX(100);
        super.setY(100);
    }

    @Override
    public void onEnable() {
        for (Map.Entry<Module, ModuleNamePosition> entry : renderMap.entrySet()) {
            ModuleNamePosition value = entry.getValue();
            value.y = 0;
            value.width = 0;
        }
    }

    @Override
    public void onRender(Render2DEvent event, boolean editing) {
        double posX = this.getX(), posY = this.getY();
        ZFontRenderer fontRenderer = FontManager.baloo18;
        List<Module> copy = ModuleManager.getModules();

        List<Module> modules = copy.stream().sorted(Comparator.comparingDouble(module -> getModuleNameWidth(fontRenderer, (Module) module)).reversed()).collect(Collectors.toList());
        double factor = 1.2;
        double fontHeight = fontRenderer.getHeight() * factor;

        double offsetY = posY;
        Iterator<Module> it = modules.iterator();
        while (it.hasNext()) {
            Module module = it.next();
            ModuleNamePosition position = this.getRenderValue(module);
            if (!module.isEnabled() && position.width <= 0.2 || (!this.showRenderModules.getValue() && module.getCategory() == Module.Category.RENDER)) {
                it.remove();
            }
        }

        this.setWidth(-getModuleNameWidth(fontRenderer, modules.get(0)));
        this.setHeight(modules.size() * fontHeight + 1);

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            ModuleNamePosition position = this.getRenderValue(module);

            String translate = module.getName();

            if (!module.getSuffix().isEmpty()) {
                translate += " " + Formatting.GRAY + "[" + module.getSuffix() + "]";
            }

            double offsetX = fontRenderer.getStringWidth(translate) + 4;

            if (position.y == 0 && module.isEnabled()) {
                position.y = offsetY;
            }

            if (i != modules.size() - 1) {
                Module next = modules.get(i + 1);
                ModuleNamePosition nextValue = this.getRenderValue(next);

                if (nextValue.y - position.y > fontHeight * 0.75 || !module.isEnabled()) {
                    position.width = AnimationSystem.interpolate(position.width, module.isEnabled() ? offsetX : 0, 0.2f);
                }
            } else {
                position.width = AnimationSystem.interpolate(position.width, module.isEnabled() ? offsetX : 0, 0.2f);
            }

            if (i != 0) {
                Module prev = modules.get(i - 1);
                ModuleNamePosition prevValue = this.getRenderValue(prev);

                if ((prevValue.width == 0 || prev.isEnabled()) && !prevValue.waitingY) {
                    position.waitingY = false;
                    position.y = AnimationSystem.interpolate(position.y, offsetY, 0.15f);
                } else {
                    position.waitingY = true;
                }
            } else {
                position.waitingY = false;
                position.y = AnimationSystem.interpolate(position.y, offsetY, 0.15f);
            }



            if ((position.width <= 0.2 && !module.isEnabled()) || (!this.showRenderModules.getValue() && module.getCategory() == Module.Category.RENDER)) {
                continue;
            }

            int alpha = (int) (255 * (position.width / offsetX));

            GL11.glColor4f(1, 1, 1, 1);

            if (this.arrayListRect.getValue()) {
                if (!ZephyrSettings.reduceShaders.getValue()) {

                    Rect.draw(posX/* + this.getWidth()*/ - position.width, position.y, position.width, fontHeight, RenderSystem.hexColor(0, 0, 0, 60), Rect.RectType.EXPAND);
                    ShaderUtils.doRectBlur(posX/* + this.getWidth()*/ - position.width, position.y, position.width, fontHeight);

                    if (arrayListShadow.getValue()) {
                        ShaderUtils.doRectBloom(posX/* + this.getWidth()*/ - position.width, position.y, position.width, fontHeight);
                    }

                } else {
                    Rect.draw(posX/* + this.getWidth()*/ - position.width, position.y, position.width, fontHeight, RenderSystem.reAlpha(this.arrayListRectColor.getRGB(i), Math.min(arrayListRectColor.getValue().getAlpha(), alpha) * 0.003921568627451F), Rect.RectType.EXPAND);

                }
            }

            if (this.arrayListOutline.getValue())
                Rect.draw(posX/* + this.getWidth()*/ - position.width, position.y, 1, fontHeight, RenderSystem.reAlpha(arrayListOutlineColor.getRGB(i), Math.min(arrayListOutlineColor.getValue().getAlpha(), alpha) * 0.003921568627451F), Rect.RectType.EXPAND);


            if (i == 0) {
                if (this.arrayListOutline.getValue())
                    Rect.draw(posX/* + this.getWidth()*/ - position.width, position.y, position.width, 1, RenderSystem.reAlpha(arrayListOutlineColor.getRGB(i), Math.min(arrayListOutlineColor.getValue().getAlpha(), alpha) * 0.003921568627451F), Rect.RectType.EXPAND);


            }

            if (i == modules.size() - 1) {
                if (this.arrayListOutline.getValue())
                    Rect.draw(posX/* + this.getWidth()*/ - position.width, position.y + fontHeight, position.width, 1, RenderSystem.reAlpha(arrayListOutlineColor.getRGB(i), Math.min(arrayListOutlineColor.getValue().getAlpha(), alpha) * 0.003921568627451F), Rect.RectType.EXPAND);
            } else {
                Module moduleNext = null;

                int idx = i + 1;

                while (idx < modules.size()) {
                    Module m = modules.get(idx);
                    if (m.isEnabled()) {
                        moduleNext = m;
                        break;
                    }

                    idx++;
                }

                ModuleNamePosition nextValue = this.getRenderValue(moduleNext);

                if (moduleNext != null && position.width > fontRenderer.getStringWidth(moduleNext.getName())) {
                    if (this.arrayListOutline.getValue())
                        Rect.draw(posX/* + this.getWidth()*/ - position.width, position.y + fontHeight, position.width - nextValue.width + 1, 1, RenderSystem.reAlpha(arrayListOutlineColor.getRGB(i), Math.min(arrayListOutlineColor.getValue().getAlpha(), alpha) * 0.003921568627451F), Rect.RectType.EXPAND);
                }
            }

            fontRenderer.drawString(translate, posX/* + this.getWidth()*/ - position.width + 2, position.y + fontHeight * 0.5 - fontRenderer.getHeight() * 0.5, RenderSystem.reAlpha(arrayListColor.getRGB(i), Math.min(alpha, arrayListColor.getValue().getAlpha()) * 0.003921568627451F));

            if (module.isEnabled())
                offsetY += fontHeight;
        }
    }

    private double getModuleNameWidth(ZFontRenderer fontRenderer, Module module) {
        String fullName = module.getName();

        if (!module.getSuffix().isEmpty())
            fullName += " " + Formatting.GRAY + "[" + module.getSuffix() + "]";


        return fontRenderer.getStringWidth(fullName);
    }

    ModuleNamePosition getRenderValue(Module module) {
        if (!renderMap.containsKey(module))
            renderMap.put(module, new ModuleNamePosition());

        return renderMap.get(module);
    }

    static class ModuleNamePosition {

        double y = 0;
        double width;
        boolean waitingY = false;
    }


}
