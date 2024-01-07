package tech.imxianyu.module.impl.movement.speed;

import net.minecraft.potion.Potion;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.movement.Speed;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.entity.PlayerUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OldNCPHop extends SubModule<Speed> {

    public OldNCPHop() {
        super("OldNCPHop");
    }
    private int stage, groundtick;
    private int level = 1;
    private double moveSpeed = 0.2873;
    private double lastDist;
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (event.isPre()) {
            double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
            double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
            lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        }
    };
    private int timerDelay;
    @Handler
    public void onMove(MoveEvent event) {
        ++timerDelay;
        timerDelay %= 5;
        if (timerDelay != 0) {
            mc.timer.timerSpeed = 1F;
        } else {
            if (PlayerUtils.isMoving2()) {
                mc.timer.timerSpeed = 1.3F;
                mc.thePlayer.motionX *= 1.0199999809265137;
                mc.thePlayer.motionZ *= 1.0199999809265137;
            }
        }

        //            this.mc.timer.timerSpeed = 1.07f;
        if (mc.thePlayer.onGround && PlayerUtils.isMoving2()) {
            groundtick++;
            stage = 2;
        }

        if (round(this.mc.thePlayer.posY - (double) ((int) this.mc.thePlayer.posY)) == round(0.138D)) {
            --mc.thePlayer.motionY;
            event.setY(event.getY() - 0.0931D);
            mc.thePlayer.posY -= 0.0931D;
        }

        if (stage == 1 && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
            stage = 2;
            moveSpeed = 1.35 * getBaseMoveSpeed() - 0.01;
        } else if (stage == 2) {
            mc.timer.timerSpeed = 0.8f;
            stage = 3;
            double motionY = 0.41;
            mc.thePlayer.motionY = motionY;
            event.setY(motionY);
            moveSpeed *= 2.149;
        } else if (stage == 3) {
            mc.timer.timerSpeed = 0.9f;
            stage = 4;
            double difference = 0.66 * (lastDist - getBaseMoveSpeed());
            moveSpeed = lastDist - difference;
        } else {
            mc.timer.timerSpeed = 1.1f;
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, mc.thePlayer.motionY, 0.0)).size() > 0 || mc.thePlayer.isCollidedVertically)
                stage = 1;
            moveSpeed = lastDist - lastDist / 159.8;
        }
        this.moveSpeed = Math.max(this.moveSpeed, getBaseMoveSpeed());

        setMotion(event, moveSpeed);
    };

    @Override
    public void onEnable() {
        groundtick = 0;
        this.stage = 0;

        if (mc.thePlayer == null || mc.theWorld == null)
            level = 0;
        else
            level = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, mc.thePlayer.motionY, 0.0)).size() > 0 || mc.thePlayer.isCollidedVertically ? 1 : 4;
    }

    @Override
    public void onDisable() {
        moveSpeed = getBaseMoveSpeed();
        level = 0;
    }

    public double getBaseMoveSpeed() {
        double baseSpeed = 0.2872D;
        if (mc.thePlayer != null && mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0D + 0.2D * (double) (amplifier + 1);
        }

        return baseSpeed;
    }

    private double round(double value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
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
