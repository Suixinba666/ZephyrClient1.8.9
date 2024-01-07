package tech.imxianyu.widget.impl;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.HSBColor;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ColorSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.widget.Widget;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ImXianyu
 * @since 6/20/2023 10:14 AM
 */
public class SpeedGraph extends Widget {

    private final List<Double> speedList = new CopyOnWriteArrayList<>();
    public NumberSetting<Integer> width = new NumberSetting<>("Width", 150, 100, 300, 1);
    public NumberSetting<Integer> height = new NumberSetting<>("Height", 50, 30, 150, 1);
    public NumberSetting<Double> thickness = new NumberSetting<>("Thickness", 2.0, 0.1, 3.0, 0.1);
    public NumberSetting<Double> smoothness = new NumberSetting<>("Smoothness", 0.5, 0.0, 1.0, 0.1);
    public NumberSetting<Double> yMultiplier = new NumberSetting("Y Multiplier", 7.0, 1.0, 20.0, 0.1);
    public BooleanSetting boarder = new BooleanSetting("Boarder", false);
    public BooleanSetting currentLine = new BooleanSetting("Current Line", false);
    public ColorSetting lineColor = new ColorSetting("Line Color", new HSBColor(0, 111, 255, 255));
    public ColorSetting boarderColor = new ColorSetting("Boarder Color", new HSBColor(255, 255, 255, 255));
    public ColorSetting backgroundColor = new ColorSetting("Background Color", new HSBColor(0, 0, 0, 150));
    public ColorSetting currentLineColor = new ColorSetting("Current Line Color", new HSBColor(0, 255, 0, 255));
    double lastSpeed;
    DecimalFormat df = new DecimalFormat("#.##");
    private int lastTick;

    public SpeedGraph() {
        super("Speed Graph");
    }

    @Override
    public void onRender(Render2DEvent event, boolean editing) {
        int width = this.width.getValue();
        int height = this.height.getValue();
        this.setWidth(width);
        this.setHeight(height);
        double posX = this.getX();
        double posY = this.getY();
        EntityPlayerSP thePlayer = mc.thePlayer;

        if (lastTick != thePlayer.ticksExisted) {
            lastTick = mc.thePlayer.ticksExisted;
            double z2 = mc.thePlayer.posZ;
            double z1 = mc.thePlayer.prevPosZ;
            double x2 = mc.thePlayer.posX;
            double x1 = mc.thePlayer.prevPosX;
            double speed = Math.sqrt((z2 - z1) * (z2 - z1) + (x2 - x1) * (x2 - x1));
            if (speed < 0) {
                speed = -speed;
            }
            speed = (lastSpeed * 0.9 + speed * 0.1) * smoothness.getValue() + speed * (1 - smoothness.getValue());
            lastSpeed = speed;
            speedList.add(speed);
            while (speedList.size() > width) {
                speedList.remove(0);
            }
        }

        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);

        Rect.draw(posX, posY, width, height, backgroundColor.getRGB(), Rect.RectType.EXPAND);

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(thickness.getValue().floatValue());

        GL11.glBegin(GL11.GL_LINE_STRIP);

        int size = this.speedList.size();
        int start = size > width ? size - width : 0;
        int i = start;
        RenderSystem.color(lineColor.getRGB());
        for (int var6 = size - 1; i < var6; ++i) {
            double y = speedList.get(i) * 10 * yMultiplier.getValue();
            double y1 = speedList.get(i + 1) * 10 * yMultiplier.getValue();
            GL11.glVertex2d(posX + i - start, posY + height + 1 - Math.min(y, height));
            GL11.glVertex2d(posX + i + 1.0 - start, posY + height + 1 - Math.min(y1, height));
        }
        GL11.glEnd();

        if (currentLine.getValue()) {
            double y = (speedList.size() == 0 ? 0 : (speedList.get(speedList.size() - 1))) * 10 * yMultiplier.getValue();
            RenderSystem.color(currentLineColor.getRGB());
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(posX, posY + height + 1 - Math.min(y, height));
            GL11.glVertex2d(posX + width, posY + height + 1 - Math.min(y, height));
            GL11.glEnd();

            FontManager.pf18.drawOutlineString(df.format(speedList.size() == 0 ? 0 : (speedList.get(speedList.size() - 1))), posX + width + 5, posY + height + 1 - Math.min(y, height) - FontManager.pf18.getHeight() / 2.0, 0xFFFFFFFF, 0xFF000000);
        }

        if (boarder.getValue()) {
            RenderSystem.color(boarderColor.getRGB());
            GL11.glBegin(GL11.GL_LINE_STRIP);
            GL11.glVertex2d(posX, posY);
            GL11.glVertex2d(posX + width, posY);
            GL11.glVertex2d(posX + width, posY + height + 2);
            GL11.glVertex2d(posX, posY + height + 2);
            GL11.glVertex2d(posX, posY);
            GL11.glEnd();
        }

        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.resetColor();
    }
}
