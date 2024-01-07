package tech.imxianyu.module.impl.movement;

import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vector3d;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.management.FriendManager;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.world.AntiBots;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.player.MoveUtils;
import tech.imxianyu.utils.rotation.RotationUtils;

import java.awt.*;

public class TargetStrafe extends Module {
    public static boolean direction = true;
    public static float currentYaw;
    public NumberSetting<Double> range = new NumberSetting<>("Range", 2.0, 0.5, 4.5, 0.1);
    public ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Adaptive);
    @Handler
    public void onTick(TickEvent event) {
        this.setSuffix(this.mode.getCurMode());

    };
    public BooleanSetting jumpKey = new BooleanSetting("Jump Key Only", true);
    public BooleanSetting renderCircle = new BooleanSetting("Render Circle", true);
    @Handler
    public void onRender3D(Render3DEvent event) {
        for (EntityPlayer ent : mc.theWorld.playerEntities) {
            this.renderCircle(ent, event.partialTicks, range.getFloatValue(), Color.GREEN.getRGB());
        }
    };
    public BooleanSetting lockPersonView = new BooleanSetting("Lock F5 View", false);
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (lockPersonView.getValue() && ModuleManager.killAura.isEnabled()) {
            if (ModuleManager.killAura.target != null && (!jumpKey.getValue() || mc.gameSettings.keyBindJump.pressed) && (ModuleManager.speed.isEnabled() || ModuleManager.fly.isEnabled())) {
                if (mc.gameSettings.thirdPersonView != 1) {
                    mc.gameSettings.thirdPersonView = 1;
                }
            } else {
                if (mc.gameSettings.thirdPersonView != 0) {
                    mc.gameSettings.thirdPersonView = 0;
                }
            }
        }
    };
    public BooleanSetting ground = new BooleanSetting("On Ground", true);
    @Handler
    public void onMove(MoveEvent event) {
        if (ground.getValue() && MoveUtils.isMoving()) {
            if (ModuleManager.killAura.target != null && !ModuleManager.speed.isEnabled()) {
                move(MoveUtils.getBaseMoveSpeed(), ModuleManager.killAura.target);
            }
        }
    };
    public NumberSetting<Double> animationRange = new NumberSetting<>("Animation Range", 0.3, 0.0, 1.0, 0.01);

    public TargetStrafe() {
        super("Target Strafe", Category.MOVEMENT);
    }

    public void renderCircle(EntityPlayer entity, float partialTicks, float range, int color) {
        if (renderCircle.getValue() && entity != mc.thePlayer && !entity.isInvisible() && mc.thePlayer.canEntityBeSeen(entity) && entity.isEntityAlive() && entity.getDistanceToEntity(mc.thePlayer) < 4 && !FriendManager.isFriend(entity) && !AntiBots.isBot(entity)) {
            GL11.glPushMatrix();
            mc.entityRenderer.disableLightmap();
            GL11.glDisable(3553);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(2929);
            GL11.glEnable(2848);
            GL11.glDepthMask(false);

            final double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
            final double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
            final double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

            GL11.glPushMatrix();
            final double tau = 6.283185307179586;
            final double fans = 45.0;
            GL11.glLineWidth(2.0f);
            GL11.glBegin(1);
            for (int i = 0; i <= 90; ++i) {
                RenderSystem.color(color);
                GL11.glVertex3d(posX + range * Math.cos(i * Math.PI * 2 / 45.0), posY, posZ + range * Math.sin(i * Math.PI * 2 / 45.0));
            }
            GL11.glEnd();
            GL11.glPopMatrix();

            GL11.glDepthMask(true);
            GL11.glDisable(2848);
            GL11.glEnable(2929);
            GL11.glDisable(3042);
            GL11.glEnable(3553);
            mc.entityRenderer.enableLightmap();
            GL11.glPopMatrix();
        }
    }

    private boolean isBlockUnder(Entity entity) {
        for (int i = (int) (entity.posY - 1.0); i > 0; --i) {
            BlockPos pos = new BlockPos(entity.posX,
                    i, entity.posZ);
            if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)
                continue;
            return true;
        }
        return false;
    }

    public void move(double speed, Entity entity) {
        if (!isBlockUnder(entity) && mode.getValue() == Mode.Adaptive) {
            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
            return;
        }
        if (!isBlockUnder(mc.thePlayer) && mode.getValue() == Mode.Adaptive && !ModuleManager.fly.isEnabled())
            direction = !direction;

        if (mc.thePlayer.isCollidedHorizontally && mode.getValue() == Mode.Adaptive)
            direction = !direction;
//
//        speed = mc.thePlayer.getSpeed();
//        speed *= 1.1;
        float strafe = direction ? 1 : -1;
        float diff = (float) (speed / (range.getValue() * Math.PI * 2)) * 360 * strafe;
        float[] rotation = RotationUtils.getNeededRotations(new Vector3d(entity.posX, entity.posY, entity.posZ), new Vector3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));

        rotation[0] += diff;
        float dir = rotation[0] * (float) (Math.PI / 180F);

        double x = entity.posX - Math.sin(dir) * range.getValue();
        double z = entity.posZ + Math.cos(dir) * range.getValue();

        float yaw = RotationUtils.getNeededRotations(new Vector3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), new Vector3d(x, entity.posY, z))[0] * (float) (Math.PI / 180F);

//        double cos = Math.cos(Math.toRadians(yaw));
//        double sin = Math.sin(Math.toRadians(yaw));
//        double fw = mc.thePlayer.movementInput.moveForward;
//        double st = mc.thePlayer.movementInput.moveStrafe;
//        mc.thePlayer.motionX = fw * speed * cos - st * speed * sin;
//        mc.thePlayer.motionZ = fw * speed * sin + st * speed * cos;

        mc.thePlayer.motionX = -MathHelper.sin(yaw) * speed;
        mc.thePlayer.motionZ = MathHelper.cos(yaw) * speed;

    }

    public enum Mode {
        Simple,
        Adaptive
    }

}
