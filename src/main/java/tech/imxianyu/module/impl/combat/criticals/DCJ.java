package tech.imxianyu.module.impl.combat.criticals;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.combat.Criticals;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.entity.PlayerUtils;

public class DCJ extends SubModule<Criticals> {


    public DCJ() {
        super("DCJ");
    }

    int stage;

    @Handler
    public void preUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        this.getModule().setSuffix(this.getName());

        if (ModuleManager.killAura.isEnabled() && ModuleManager.killAura.target != null && this.getModule().canCrit(ModuleManager.killAura.target) && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.onGround) {
            if (!(ModuleManager.speed.isEnabled() || ModuleManager.fly.isEnabled() || ModuleManager.blockFly.isEnabled())) {
                mc.thePlayer.posY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                mc.thePlayer.lastTickPosY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                mc.thePlayer.cameraYaw = mc.thePlayer.cameraPitch = PlayerUtils.isMoving2() ? 0.1F : 0f;
            }
        }
    }

    @Handler
    public void move(MoveEvent event) {
        if (ModuleManager.killAura.isEnabled() && ModuleManager.killAura.target != null && !ModuleManager.blockFly.isEnabled()) {
            if (isOnGround(0.01) && (stage >= 0 || mc.thePlayer.isCollidedHorizontally) && !ModuleManager.blockFly.isEnabled() && (!ModuleManager.autoEat.isEnabled() || !ModuleManager.autoEat.eating)) {
                stage = 0;
                final double y = .4001999986886975 + getJumpEffect() * .099;
                event.setY(y);
            }
            setMotion(event, getBaseMovementSpeed());
            ++stage;
        }
    }

    @Handler
    public void onAttack(SendPacketEvent event) {
        if (event.getPacket() instanceof C03PacketPlayer && !ModuleManager.blockFly.isEnabled()) {
            C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
            packet.onGround = false;
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                packet.y = (packet.y + 0.01);
            }
        }
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

    public double getBaseMovementSpeed() {
        double baseSpeed = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            final int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

    public int getJumpEffect() {
        if (mc.thePlayer.isPotionActive(Potion.jump))
            return mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1;
        else
            return 0;
    }

    public boolean isOnGround(final double n) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -n, 0.0)).isEmpty();
    }

}
