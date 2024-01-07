package tech.imxianyu.module.impl.movement.speed;

import net.minecraft.block.state.pattern.BlockHelper;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.movement.Speed;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.utils.player.BlockUtils;

import java.util.List;

/**
 * @author ImXianyu
 * @since 7/5/2023 7:06 PM
 */
public class AntiNCP extends SubModule<Speed> {

    public AntiNCP() {
        super("AntiNCP");
    }
    public BooleanSetting damageBoost = new BooleanSetting("Damage Boost", false);

    private double speed;
    private int stage;

    @Override
    public void onEnable() {
        stage = 0;
    }

    @Handler
    public void onMove(MoveEvent e) {
        if ((mc.thePlayer.moveForward == 0.0F) && (mc.thePlayer.moveStrafing == 0.0F)) {
            speed = 0.29;
        }
        if ((stage == 1) && (mc.thePlayer.isCollidedVertically) && ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F))) {
            speed = 3.4 * getBaseMovementSpeedForNCP() - 0.01;
        } else if (!BlockUtils.isInLiquid() && !BlockUtils.isOnLadder() && (stage == 2) && (mc.thePlayer.isCollidedVertically) && isOnGround(0.001) && ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F))) {
            this.mc.timer.timerSpeed = 1.0f;
            e.setY(mc.thePlayer.motionY = 0.3999999D);
            speed *= 2.149D;
        } else if (stage == 3) {
            double difference = 0.66 * (this.speed - getBaseMovementSpeedForNCP());
            if (damageBoost.getValue()) {
                if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.hurtTime < 10)
                    difference *= 0.8f;
            }
            speed -= difference;
//                speed -= 0.05;
        } else {
            this.mc.timer.timerSpeed = 0.9f;
            List<AxisAlignedBB> collidingList = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0D, mc.thePlayer.motionY, 0.0D));
            if ((collidingList.size() > 0) || (mc.thePlayer.isCollidedVertically)) {
                if (stage > 0) {
                    if (1.35D * getBaseMovementSpeedForNCP() - 0.01D > speed) {
                        stage = 0;
                    } else {
                        stage = (mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F) ? 1 : 0;
                    }
                }
            }
            speed = this.speed - this.speed / 159.0D;
        }
        speed = Math.max(speed, getBaseMovementSpeedForNCP());
        if (stage > 0) {
            setMotion(e, this.speed);
        }
        if ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F)) {
            stage += 1;
        }
    };

    public boolean isOnGround(final double n) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -n, 0.0)).isEmpty();
    }

    public double getBaseMovementSpeedForNCP() {
        double baseSpeed = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.25 * (double)(amplifier + 1);
        }
        return baseSpeed;
    }

    public void setMotion(MoveEvent em, double speed) {
        if (!ModuleManager.targetStrafe.ground.getValue() && ModuleManager.speed.isEnabled() && ModuleManager.targetStrafe.isEnabled() && (!ModuleManager.targetStrafe.jumpKey.getValue() || mc.gameSettings.keyBindJump.isKeyDown())) {
            if (ModuleManager.killAura.target != null) {
                ModuleManager.targetStrafe.move(speed, ModuleManager.killAura.target);
                return;
            }
        }

        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if ((forward == 0.0D) && (strafe == 0.0D)) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            if (em != null) {
                em.setX(0.0D);
                em.setZ(0.0D);
            }
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1;
                } else if (forward < 0.0D) {
                    forward = -1;
                }
            }
            double cos = Math.cos(Math.toRadians(yaw + 90));
            double sin = Math.sin(Math.toRadians(yaw + 90));
            mc.thePlayer.motionX = forward * speed * cos + strafe * speed * sin;
            mc.thePlayer.motionZ = forward * speed * sin - strafe * speed * cos;
            if (em != null) {
                em.setX(mc.thePlayer.motionX);
                em.setZ(mc.thePlayer.motionZ);
            }
        }
    }
}
