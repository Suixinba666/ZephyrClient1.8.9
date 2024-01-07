package tech.imxianyu.gui.selectworld;

import lombok.SneakyThrows;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatComparator;
import org.lwjglx.input.Keyboard;
import tech.imxianyu.gui.ZephyrScreen;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.BloomRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ImXianyu
 * @since 5/1/2023 2:09 PM
 */
public class ZephyrSelectWorld extends ZephyrScreen {

    private final List<WorldBean> worldBeans = new ArrayList<>();
    private final GuiScreen parentScreen;
    private List<SaveFormatComparator> listSaves;

    public ZephyrSelectWorld(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    @SneakyThrows
    public void initGui() {
        this.loadLevelList();
        this.worldBeans.clear();

        double offsetX = 30, offsetY = 70;
        double width = 150, height = 50;
        int radius = 5;
        double xSpace = 20, ySpace = 20;

        int count = 0;
        int horizontalLength = (int) ((RenderSystem.getWidth() - 100) / (width + xSpace));


        for (SaveFormatComparator listSave : this.listSaves) {
            this.worldBeans.add(new WorldBean(listSave, offsetX, offsetY, width, height, radius));

            offsetX += width + xSpace;


            if (count == horizontalLength) {
                count = 0;
                offsetX = 30;
                offsetY += ySpace + height;
            }

            ++count;
        }
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), ColorUtils.getColor(ColorUtils.ColorType.Base), Rect.RectType.EXPAND);

//        BloomRenderer.preRenderShadow();
//
//        for (WorldBean worldBean : this.worldBeans) {
//            RoundedRect.drawRound(worldBean.getX(), worldBean.getY() - worldBean.yAnimation, worldBean.getWidth(), worldBean.getHeight(), worldBean.getRadius(), Color.WHITE);
//        }
//
//        BloomRenderer.postRenderShadow();

        for (WorldBean worldBean : this.worldBeans) {
            worldBean.draw(mouseX, mouseY);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            mc.displayGuiScreen(this.parentScreen);
        }
    }

    private void loadLevelList() throws AnvilConverterException {
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        this.listSaves = isaveformat.getSaveList();
        Collections.sort(this.listSaves);
    }
}
