package tech.imxianyu.module.impl.render;

import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.events.world.WorldChangedEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.HSBColor;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ColorSetting;
import tech.imxianyu.settings.NumberSetting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author ImXianyu
 * @since 6/29/2023 5:11 PM
 */
public class BreadCrumbs extends Module {

    public BooleanSetting fadeOut = new BooleanSetting("Fade Out", false);
    public BooleanSetting autoRemove = new BooleanSetting("Auto Remove", false);
    public NumberSetting<Double> removeDistance = new NumberSetting<>("Remove Distance", 20.0, 1.0, 40.0, 0.1, autoRemove::getValue);
    public NumberSetting<Double> lineWidth = new NumberSetting<>("Line Width", 2.0, 1.0, 8.0, 0.1);


    public ColorSetting crumbColor = new ColorSetting("Crumb Color", new HSBColor(255, 255, 255, 255));
    List<Vec3> positions = new ArrayList<>();
    @Handler
    public void onMove(MoveEvent event) {
        if (event.getX() != 0.0 || event.getY() != 0.0 || event.getZ() != 0.0) {
            this.positions.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
        }
    };
    @Handler
    public void onWorldChanged(WorldChangedEvent event) {
        this.positions.clear();
    };
    @Handler
    public void onRender3D(Render3DEvent event) {

        Vec3 playerPos = new Vec3(mc.thePlayer.lastTickPosX - (mc.thePlayer.lastTickPosX - mc.thePlayer.posX) * mc.timer.renderPartialTicks, mc.thePlayer.lastTickPosY - (mc.thePlayer.lastTickPosY - mc.thePlayer.posY) * mc.timer.renderPartialTicks, mc.thePlayer.lastTickPosZ - (mc.thePlayer.lastTickPosZ - mc.thePlayer.posZ) * mc.timer.renderPartialTicks);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glLineWidth(lineWidth.getFloatValue());
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        RenderSystem.color(RenderSystem.reAlpha(this.crumbColor.getRGB(), 0.5f));
        GL11.glBegin(3);

        int count = 0;

        Iterator<Vec3> iterator = this.positions.iterator();

        while (iterator.hasNext()) {
            Vec3 pos = iterator.next();

            Vec3 renderPos = this.getRenderPosition(pos);
            double distance = pos.distanceTo(playerPos);

            if (autoRemove.getValue() && distance > removeDistance.getValue())
                iterator.remove();

            float alpha = !this.fadeOut.getValue() ? 0.6000000238418579f : (float) (1.0 - Math.min(1.0, distance / 20.0));
//            if (!(distance > 24.0)) {
                RenderSystem.color(RenderSystem.reAlpha(this.crumbColor.getRGB(count), alpha));
                GL11.glVertex3d(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord);
//            }

            count++;

        }

        Vec3 endPos = this.getRenderPosition(playerPos);
        GL11.glVertex3d(endPos.xCoord, endPos.yCoord, endPos.zCoord);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
    };

    public BreadCrumbs() {
        super("Bread Crumbs", Category.RENDER);
    }

    @Override
    public void onEnable() {
        this.positions.clear();
    }

    public Vec3 getRenderPosition(Vec3 from) {
        return from.add(new Vec3(-mc.getRenderManager().renderPosX, -mc.getRenderManager().renderPosY, -mc.getRenderManager().renderPosZ));
    }
}
