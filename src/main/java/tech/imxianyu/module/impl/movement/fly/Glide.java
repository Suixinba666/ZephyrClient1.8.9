package tech.imxianyu.module.impl.movement.fly;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.entity.PlayerUtils;

/**
 * @author ImXianyu
 * @since 1/28/2023 9:32 PM
 */
public class Glide extends SubModule {

    public Glide() {
        super("Glide");
    }
    public NumberSetting<Double> speed = new NumberSetting<>("Glide Speed", 0.28, 0.1, 3.0, 0.1);
    @Handler
    public void onMove(MoveEvent event) {
        if (!PlayerUtils.isMoving2()) {
            this.setMotion(event, 0);
            return;
        }

        if (mc.thePlayer.onGround && mc.thePlayer.motionY < 0) {
            this.getModule().toggle();
            return;
        }

        if (event.y < 0) {
            event.y = Math.max(-0.05, event.y);
        }

        this.setMotion(event, this.speed.getValue());
    };

    @Override
    public void onEnable() {
        //Jump Start
        mc.thePlayer.motionY = 0.42F;
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
