package tech.imxianyu.module.impl.player;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import tech.imxianyu.eventapi.EnumPriority;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.eventapi.Priority;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.rendering.RenderPlayerRotationsEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.rotation.RotationUtils;

import java.text.DecimalFormat;
import java.util.Random;

public class HeadRotations extends Module {

    public HeadRotations() {
        super("Head Rotations", Category.PLAYER);
    }

    public ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.AntiAiming);
    public enum Mode {
        AntiAiming,
        CircleAround;
    }


    public BooleanSetting onlyAntiPlayers = new BooleanSetting("Anti Players", true, () -> this.mode.getValue() == Mode.AntiAiming);
    public NumberSetting<Float> yawOffset = new NumberSetting<Float>("Yaw Offset", 180.0f, 0.0f, 360.0f, 0.1f);
    public NumberSetting<Float> pitch = new NumberSetting<Float>("Pitch", 90.0f, -90.0f, 90.0f, 0.1f);
    public NumberSetting<Float> yawAngle = new NumberSetting<Float>("Yaw Angle", 10.0f, 0.0f, 360.0f, 0.1f, () -> this.mode.getValue() == Mode.AntiAiming);
    public NumberSetting<Float> yawSwitchTicks = new NumberSetting<Float>("Yaw Switch Ticks", 4.0f, 0.0f, 10.0f, 1.0f, () -> this.mode.getValue() == Mode.AntiAiming);
    public NumberSetting<Float> yawRandom = new NumberSetting<Float>("Yaw Random Angle", 0.01f, 0.01f, 360.0f, 0.1f, () -> this.mode.getValue() == Mode.AntiAiming);
    public NumberSetting<Float> pitchRandom = new NumberSetting<Float>("Pitch Random Angle", 0.01f, 0.01f, 180.0f, 0.1f, () -> this.mode.getValue() == Mode.AntiAiming);
    public NumberSetting<Float> thetaValue = new NumberSetting<Float>("Theta", 1f, 1f, 20f, 1f, () -> this.mode.getValue() == Mode.CircleAround);
    private boolean switchAngle = false;
    private int ticks = 0;
    private final Random random = new Random();
    private float lastYaw = 0, lastPitch = 0, theta = 0;
    DecimalFormat df = new DecimalFormat("##.#");

    @Priority(priority = EnumPriority.HIGHEST)
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;


        if (this.mode.getValue() == Mode.AntiAiming) {
            EntityPlayer nearest = this.getNearestPlayer();
            float yaw = mc.thePlayer.rotationYaw;
            float pitch = mc.thePlayer.rotationPitch;
            ++this.ticks;
            if ((float)this.ticks >= yawSwitchTicks.getValue()) {
                this.ticks = 0;
                this.switchAngle = !this.switchAngle;
            }
            if (onlyAntiPlayers.getValue() && (nearest != null && (double)mc.thePlayer.getDistanceToEntity(nearest) <= 10.0)) {
                float[] rot = RotationUtils.getRotations(nearest.posX, nearest.posY + (double)nearest.getEyeHeight(), nearest.posZ);
                yaw = rot[0];
            }

            float v = yaw + this.yawOffset.getValue() + yawAngle.getValue() * (float) (this.switchAngle ? -1 : 1) + (float) random.nextInt((int) Math.max(yawRandom.getValue() * 1000.0f, 1)) / 1000.0f * (float) (this.switchAngle ? -1 : 1);
            float w = this.pitch.getValue() + (float) random.nextInt((int) Math.max(pitchRandom.getValue() * 1000.0f, 1)) / 1000.0f * (float) (this.switchAngle ? -1 : 1);

            event.setRotationYaw(v);
            event.setRotationPitch(w);
            lastYaw = v;
            lastPitch = w;
//        event.modified = false;
            this.setSuffix("AA Yaw [" + this.yawOffset.getValue() + ", " + yawAngle.getValue() + "] Pitch [" + this.pitch.getValue() + "] , Rand [" + yawRandom.getValue() + ", " + pitchRandom.getValue() + "], Ticks [" + yawSwitchTicks.getValue() + "]");
        } else if (this.mode.getValue() == Mode.CircleAround) {
            this.setSuffix("CA");
            float yaw = mc.thePlayer.rotationYaw;
            float v = yaw + this.yawOffset.getValue() + theta;
            float w = this.pitch.getValue();

            event.setRotationYaw(v);
            event.setRotationPitch(w);
            lastYaw = v;
            lastPitch = w;
            this.theta += this.thetaValue.getValue();
        }
    };

    @Priority(priority = EnumPriority.HIGHEST)
    @Handler
    public void onRotation(RenderPlayerRotationsEvent event) {
        event.setRotationYaw(lastYaw);
        event.setRotationPitch(lastPitch);
    };

    private EntityPlayer getNearestPlayer() {
        if (mc.theWorld.playerEntities.size() == 1) {
            return null;
        }
        EntityPlayer nearest = mc.theWorld.playerEntities.get(1);
        for (EntityPlayer entity : mc.theWorld.playerEntities) {
            if (entity == mc.thePlayer || entity instanceof EntityPlayerSP || !(mc.thePlayer.getDistanceToEntity(entity) < mc.thePlayer.getDistanceToEntity(nearest))) continue;
            nearest = entity;
        }
        return nearest;
    }

}
