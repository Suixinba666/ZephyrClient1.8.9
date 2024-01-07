package tech.imxianyu.module.impl.movement.speed;

import net.minecraft.potion.Potion;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.movement.Speed;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.BooleanSetting;

/**
 * @author ImXianyu
 * @since 7/5/2023 7:01 PM
 */
public class NCPFast extends SubModule<Speed> {

    public NCPFast() {
        super("NCPFast");
    }
    public BooleanSetting damageBoost = new BooleanSetting("Damage Boost", false);

    private int air = 0;

    @Handler
    public void onMove(MoveEvent e) {
        if (isOnGround(0.01) && isMoving()) {
//                int i = 0;
//                while (i < 20) {
//                    this.mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(this.mc.thePlayer.posX, this.mc.thePlayer.posY + 1.0E-9, this.mc.thePlayer.posZ, this.mc.thePlayer.onGround));
//                    ++i;
//                }
            air = -1;
//            e.y = mc.thePlayer.motionY = 0.4 + randomNumber(-10000, 0) * 1e-10;
        }
        if (isMoving()) {
            air++;
            double speed = 0.7 + getSpeedEffect() * 0.2 + (double) air / 35;
            if (this.mc.thePlayer.hurtTime > 0 && this.mc.thePlayer.hurtTime <= 10 && damageBoost.getValue()) speed += 0.15f;
            setMotion(e, speed);
        }
    };

    public boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F);
    }

    public boolean isOnGround(final double n) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -n, 0.0)).isEmpty();
    }

    public int randomNumber(final int n, final int n2) {
        return Math.round(n2 + (float)Math.random() * (n - n2));
    }

    public int getSpeedEffect() {
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
        }
        return 0;
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
