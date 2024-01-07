package tech.imxianyu.module.impl.movement.speed;

import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.movement.Speed;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.timing.Timer;

import java.time.Duration;

/**
 * @author ImXianyu
 * @since 2023/12/3
 */
public class Strafe extends SubModule<Speed> {

    public Strafe() {
        super("Strafe");
    }

    int groundTick = 0;

    final Timer boostTimer = new Timer();

    public final NumberSetting<Double> damageBoostTime = new NumberSetting<>("Damage Boost Time", 2.0, 0.0, 10.0, 0.5) {
        @Override
        public String getStringForRender() {
            return super.getStringForRender() + " Sec";
        }
    };

    @Override
    public void onEnable() {
        groundTick = 0;
    }

    @Handler
    public void onMove(MoveEvent event) {
        if (isMoving()) {

            if (mc.thePlayer.hurtTime > 0) {
                this.boostTimer.reset();
            }

//            mc.thePlayer.addChatMessage(String.valueOf(mc.thePlayer.getSpeed()));
            this.setMotion(event, Math.min(2, Math.max(mc.thePlayer.getSpeed(), this.getBaseMoveSpeed()) * (this.boostTimer.isDelayed((long) (damageBoostTime.getValue() * 1000L)) ? 1 : 1.125)));

            if (mc.thePlayer.onGround) {
//                double motionY = 0.41;
//                mc.thePlayer.motionY = motionY;
//                event.setY(motionY);

                groundTick ++;

                mc.thePlayer.motionY = mc.thePlayer.getJumpUpwardsMotion();
                event.setY(mc.thePlayer.getJumpUpwardsMotion());

                if (groundTick == 1) {
                    return;
                }

                if (mc.thePlayer.isPotionActive(Potion.jump)) {
                    event.y += (float) (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                }

                if (mc.thePlayer.isSprinting()) {
                    float f = mc.thePlayer.rotationYaw * 0.017453292F;
                    this.setMotion(event, getBaseMoveSpeed() * (this.mc.thePlayer.hurtTime > 0 && this.mc.thePlayer.hurtTime <= 10 ? 2.75 : 2.125));
                }
            }
        }
    }

    public boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F);
    }

    public boolean isOnGround(final double n) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -n, 0.0)).isEmpty();
    }

    public double getBaseMoveSpeed() {
        double baseSpeed = 0.2872D;
        if (mc.thePlayer != null && mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0D + 0.2D * (double) (amplifier + 1);
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
