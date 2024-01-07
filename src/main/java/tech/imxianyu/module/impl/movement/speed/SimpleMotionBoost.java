package tech.imxianyu.module.impl.movement.speed;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.AttackEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.utils.timing.Timer;

public class SimpleMotionBoost extends SubModule {

    public SimpleMotionBoost() {
        super("SimpleMotionBoost");
    }

    public NumberSetting<Double> motionSpeed = new NumberSetting<>("Normal Speed", 0.2872, 0.0, 1.0, 0.01);
    public BooleanSetting escapeMode = new BooleanSetting("Escape Mode", false);
    public NumberSetting<Double> escapeSpeed = new NumberSetting<>("Escape Speed", 2.5, 0.0, 1.0, 0.01, () -> escapeMode.getValue());
    public BooleanSetting jump = new BooleanSetting("Jump", true);
    Timer escapeTimer = new Timer();
    double speed;

    boolean canEscape = false;
    @Handler
    public void onAttack(AttackEvent event) {
        this.escapeTimer.reset();
        canEscape = false;

        if (escapeMode.getValue())
            speed = motionSpeed.getValue();
    };
    @Handler
    public void onTick(TickEvent event) {
        if (!event.isPre())
            return;

        if (!PlayerUtils.isMoving2())
            return;

        if (!jump.getValue())
            return;

        Block block = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)).getBlock();

        if (mc.thePlayer.onGround && block.isFullBlock()) {
            if (escapeTimer.isDelayed(100) && escapeMode.getValue()) {
                canEscape = true;
            }
            mc.thePlayer.jump();
        }
    };
    @Handler
    public void anotherOnTick(TickEvent event) {
        if (!event.isPre())
            return;

        if (!PlayerUtils.isMoving2()) {
            this.setMotion(0);
            return;
        }

        speed = AnimationSystem.interpolate(speed, (canEscape ? escapeSpeed.getValue() : motionSpeed.getValue()), 0.1f);
        Block block = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)).getBlock();
        if (block.isFullBlock() || block == Blocks.air) {
            this.setMotion(speed);
        } else if (block instanceof BlockStairs) {
            this.setMotion(getBaseMovementSpeed());
            speed = getBaseMovementSpeed() * 2;
        }
    };
    Timer jumpDelay = new Timer();

    @Override
    public void onEnable() {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        speed = getBaseMovementSpeed() * 2;
    }

    public double getBaseMovementSpeed() {
        double baseSpeed = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double) (amplifier + 1);
        }

        return baseSpeed;
    }

    public void setMotion(double speed) {
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
        }
    }
}
