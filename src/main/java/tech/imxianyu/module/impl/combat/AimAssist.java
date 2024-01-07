package tech.imxianyu.module.impl.combat;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Mouse;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.input.MouseXYChangeEvent;
import tech.imxianyu.management.FriendManager;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.world.AntiBots;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.math.MathUtils;
import tech.imxianyu.utils.player.InvUtils;
import tech.imxianyu.utils.rotation.RotationUtils;
import tech.imxianyu.utils.timing.Timer;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AimAssist extends Module {
    public static ModeSetting<TargetSwitchMode> targetSwitchMode = new ModeSetting<>("Target Switch Mode", TargetSwitchMode.Health);
    public static NumberSetting<Float> Hspeed = new NumberSetting<>("Speed (H)", 0.4f, 0.1f, 3.0f, 0.1f);
    public static NumberSetting<Float> Vspeed = new NumberSetting<>("Speed (V)", 0.4f, 0.1f, 3.0f, 0.1f);
    public static NumberSetting<Float> range = new NumberSetting<>("Range", 4.2f, 1.0f, 6.0f, 0.1f);
    public static NumberSetting<Float> fov = new NumberSetting<>("FoV", 60.0f, 1.0f, 360.0f, 10f);
    public static BooleanSetting random = new BooleanSetting("Random", false);
    public static BooleanSetting AttackInvisible = new BooleanSetting("Invisible", true);
    public static BooleanSetting serverTeamsCheck = new BooleanSetting("Server Teams Check", true);
    public static BooleanSetting mouseDown = new BooleanSetting("Requires Mouse Down", true);
    public static BooleanSetting checksword = new BooleanSetting("Check Weapon", true);
    public static BooleanSetting aimWhileOnTarget = new BooleanSetting("Aim While On Target", false);
    private final Timer speedLimit = new Timer();
    public NumberSetting<Float> randomValue = new NumberSetting<>("Random Value", 3.0f, 0.0f, 20.0f, 0.1f);
    Random rand = new Random();
    float[] startRot = new float[2];
    private EntityLivingBase target;
    @Handler
    public void onMouseXYChange(MouseXYChangeEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            target = null;
            return;
        }


        if (checksword.getValue() && !InvUtils.hasWeapon()) {
            target = null;
            return;
        }

//        System.out.println(this.target == null);

        if (this.target == null/*true*/) {
//            System.out.println("Get");
            EntityPlayer t = this.getTarget();

            if (this.target == null && t != null) {
                startRot[0] = mc.thePlayer.rotationYaw;
                startRot[1] = mc.thePlayer.rotationPitch;
            }

            this.target = t;
        }

//        if (/*!speedLimit.isDelayed(1) || */!isVaildToAttack((EntityPlayer) this.target)) {
//            target = null;
//            return;
//        }

        if (!aimWhileOnTarget.getValue() && this.mc.objectMouseOver.entityHit != null && mc.objectMouseOver.entityHit == target) {
            target = null;
            return;
        }

        if (mouseDown.getValue() && !Mouse.isButtonDown(0)) {
            target = null;
            return;
        }

        if (mc.currentScreen != null) {
            target = null;
            return;
        }

        if (target == null)
            return;


        try {
            float yaw = getLoserRotation(this.target)[0];
            float targetYaw = mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw);
            float targetPitch = getLoserRotation(this.target)[1];
            float random = 0;

            if (AimAssist.random.getValue()) {
                random = this.generateRandomFloat(this.randomValue.getValue() * 5);
            }

//            System.out.println(target.getName() + ", tYaw: " + targetYaw);


            event.deltaX = (int) AnimationSystem.interpolate(event.deltaX * 10000, ((targetYaw + random) - mc.thePlayer.rotationYaw) * 10000, Math.max((mc.thePlayer.rotationYaw / targetYaw), 1) * Hspeed.getValue() / 2.0) / 10000;
            event.deltaY = (int) AnimationSystem.interpolate(event.deltaY * 10000, -((targetPitch + random) - mc.thePlayer.rotationPitch) * 10000, Math.max((mc.thePlayer.rotationPitch / targetPitch), 1) * Vspeed.getValue() / 2.0) / 10000;

            speedLimit.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public AimAssist() {
        super("Aim Assist", Category.COMBAT);
    }

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float angle3 = Math.abs((angle1 - angle2)) % 360.0f;
        if (angle3 > 180.0f) {
            angle3 = 0.0f;
        }
        return angle3;
    }

    /**
     * @param bound bound
     * @return A float that is between negative bound to positive bound.
     */
    public float generateRandomFloat(float bound) {
        return (rand.nextInt((int) (bound * 1000)) / 1000.0f) * (rand.nextBoolean() ? -1 : 1);
    }

    @Override
    public void onEnable() {
        this.target = null;
    }

    public float[] getRotations(Entity entity) {
        if (entity == null) {
            return null;
        }
        double diffX = entity.posX - mc.thePlayer.posX;
        double diffZ = entity.posZ - mc.thePlayer.posZ;
        double diffY;
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase elb = (EntityLivingBase) entity;
            diffY = elb.posY + (elb.getEyeHeight() / 2.0) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        } else {
            diffY = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }


    private int randomNumber() {
        return -1 + (int) (Math.random() * 3.0);
    }

    private boolean isVaildToAttack(EntityPlayer entity) {
        if (entity == null || entity.isDead || entity.getHealth() <= 0) {
//            System.out.println(entity.getName() + ": Null Dead <0");
            return false;
        }

        if (mc.thePlayer.getDistanceToEntity(entity) > range.getValue()) {
//            System.out.println(entity.getName() + ": Range");
            return false;
        }

        if (entity.isInvisible() && !AttackInvisible.getValue()) {
//            System.out.println("Invisible");
            return false;
        }


        if (fov.getValue() != 360f && !RotationUtils.isVisibleFOV(entity, fov.getValue())) {
//            System.out.println("FoV");
            return false;
        }

        if (ModuleManager.antiBots.isEnabled() && AntiBots.isBot(entity)) {
//            System.out.println("Bot");
            return false;
        }

        if (entity == mc.thePlayer)
            return false;

        if (FriendManager.isFriend(entity))
            return false;

        //            System.out.println("Can't Attack");
        return /*mc.thePlayer.canAttackPlayer(entity) || */!serverTeamsCheck.getValue();
    }

    private Comparator<EntityPlayer> getComparator() {
        switch (targetSwitchMode.getValue()) {
            case Distance:
                return Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e));
            case Health:
                return Comparator.comparingDouble(EntityLivingBase::getHealth);
            case Armor:
                return Comparator.comparingInt(o -> o.inventory.getTotalArmorValue());
            case HurtTime:
                return Comparator.comparingInt(o -> o.hurtResistantTime);
            case FoV:
                return Comparator.comparingDouble(o -> getDistanceBetweenAngles(mc.thePlayer.rotationPitch, getLoserRotation(o)[0]));
        }
        return Comparator.comparingDouble(EntityLivingBase::getHealth);
    }

    public float[] getLoserRotation(Entity target) {
        double xDiff = target.posX - mc.thePlayer.posX;
        double yDiff = target.posY - mc.thePlayer.posY - 0.4;
        double zDiff = target.posZ - mc.thePlayer.posZ;

        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0 / 3.141592653589793) - 90.0f;
        float pitch = (float) ((-Math.atan2(yDiff, dist)) * 180.0 / 3.141592653589793);
        float[] array = new float[2];

        float rotationYaw = mc.thePlayer.rotationYaw;

        array[0] = rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw);

        float rotationPitch = mc.thePlayer.rotationPitch;
        array[1] = rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch);
        return array;
    }

    private EntityPlayer getTarget() {
        List<EntityPlayer> players = mc.theWorld.playerEntities.stream().filter(this::isVaildToAttack).sorted(
                this.getComparator()
        ).collect(Collectors.toList());

        if (players.size() == 0) {
            return null;
        }

        return players.get(0);
    }

    public float[] getRotationsEntity(Entity entity) {
        return getRotations(entity.posX + MathUtils.randomNumber(0.1, -0.1), entity.posY + (double) entity.getEyeHeight() - 0.4 + MathUtils.randomNumber(0.7, -0.7), entity.posZ + MathUtils.randomNumber(0.5, -0.5));
    }

    public float[] getRotations(double posX, double posY, double posZ) {
        EntityPlayerSP player = mc.thePlayer;
        double x = posX - player.posX;
        double y = posY - (player.posY + (double) player.getEyeHeight());
        double z = posZ - player.posZ;
        double dist = MathHelper.sqrt_double(x * x + z * z);
        float aYaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float aPitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[]{aYaw, aPitch};
    }

    private void faceTarget(Entity target, float yawspeed, float pitchspeed) {
        EntityPlayerSP player = this.mc.thePlayer;
        float yaw = getRotationsEntity(target)[0];
        float pitch = getRotationsEntity(target)[1];
        player.rotationYaw = this.getRotation(player.rotationYaw, yaw, yawspeed);
        player.rotationPitch = this.getRotation(player.rotationPitch, pitch, pitchspeed);
    }

    protected float getRotation(float currentRotation, float targetRotation, float maxIncrement) {

        return AnimationSystem.interpolate(currentRotation, MathHelper.wrapAngleTo180_float(targetRotation - currentRotation) + new Random().nextInt(20), maxIncrement / 10.0f);
    }

    public enum TargetSwitchMode {
        HurtTime,
        Distance,
        Health,
        Armor,
        FoV
    }
}
