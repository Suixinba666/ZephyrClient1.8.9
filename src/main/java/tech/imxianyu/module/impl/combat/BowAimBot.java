package tech.imxianyu.module.impl.combat;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.MathHelper;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.management.FriendManager;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.world.AntiBots;
import tech.imxianyu.settings.BooleanSetting;

import java.util.ArrayList;
import java.util.Comparator;

public class BowAimBot extends Module {
    public static ArrayList<Entity> attackList = new ArrayList<>();
    public static ArrayList<Entity> targets = new ArrayList<>();
    public static int currentTarget;

    static {
        BowAimBot.attackList = new ArrayList<>();
    }

    private final BooleanSetting lockView = new BooleanSetting("Lock View", false);
    @Handler
    public void onUpdate(UpdateEvent pre) {
        if (!pre.isPre()) {
            return;
        }

        if (mc.thePlayer.inventory.getCurrentItem() == null || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBow))
            return;


        for (Entity o : mc.theWorld.loadedEntityList) {
            if (((o instanceof EntityPlayer)) && (!targets.contains(o))) {
                targets.add(o);
            }
            if ((targets.contains(o)) && ((o instanceof EntityPlayer))) {
                targets.remove(o);
            }
        }
        if (currentTarget >= attackList.size())
            currentTarget = 0;

        for (Entity o : mc.theWorld.loadedEntityList) {
            if ((isValidTarget(o)) && (!attackList.contains(o)))
                attackList.add(o);
        }

        attackList.removeIf(o -> !isValidTarget(o));

        this.sortTargets();


        if (mc.thePlayer != null && BowAimBot.attackList.size() != 0 && BowAimBot.attackList.get(BowAimBot.currentTarget) != null && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
            final int bowCurrentCharge = mc.thePlayer.getItemInUseDuration();
            float bowVelocity = bowCurrentCharge / 20.0f;
            bowVelocity = (bowVelocity * bowVelocity + bowVelocity * 2.0f) / 3.0f;
            bowVelocity = MathHelper.clamp_float(bowVelocity, 0.0f, 1.0f);
            final double v = bowVelocity * 3.0f;
            final double g = 0.05000000074505806;
            if (bowVelocity < 0.1) {
                return;
            }
            if (bowVelocity > 1.0f) {
                bowVelocity = 1.0f;
            }
            final double xDistance = BowAimBot.attackList.get(BowAimBot.currentTarget).posX - mc.thePlayer.posX + (BowAimBot.attackList.get(BowAimBot.currentTarget).posX - BowAimBot.attackList.get(BowAimBot.currentTarget).lastTickPosX) * (bowVelocity * 10.0f);
            final double zDistance = BowAimBot.attackList.get(BowAimBot.currentTarget).posZ - mc.thePlayer.posZ + (BowAimBot.attackList.get(BowAimBot.currentTarget).posZ - BowAimBot.attackList.get(BowAimBot.currentTarget).lastTickPosZ) * (bowVelocity * 10.0f);
            final double trajectoryXZ = Math.sqrt(xDistance * xDistance + zDistance * zDistance);
            final float trajectoryTheta90 = (float) (Math.atan2(zDistance, xDistance) * 180.0 / 3.141592653589793) - 90.0f;
            final float bowTrajectory = (float) ((float) (-Math.toDegrees(this.getLaunchAngle((EntityLivingBase) BowAimBot.attackList.get(BowAimBot.currentTarget), v, g))) - 3.8);
            if (trajectoryTheta90 <= 360.0f && bowTrajectory <= 360.0f) {

                if (this.lockView.getValue()) {
                    mc.thePlayer.rotationYaw = trajectoryTheta90;
                    mc.thePlayer.rotationPitch = bowTrajectory;
                } else {
                    pre.setRotationYaw(trajectoryTheta90);
                    pre.setRotationPitch(bowTrajectory);
                }
            }
        }
    };

    public BowAimBot() {
        super("Bow Aimbot", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        attackList = new ArrayList<>();
        targets = new ArrayList<>();
    }

    public boolean isValidTarget(final Entity entity) {
        boolean valid;
        if (entity == mc.thePlayer.ridingEntity) {
            return false;
        }

        if (!mc.thePlayer.canEntityBeSeen(entity)) {
            return false;
        }

        if (!(entity instanceof EntityPlayer))
            return false;

        if (entity instanceof EntityPlayerSP)
            return false;

        valid = mc.thePlayer.getDistanceToEntity(entity) <= 50.0f && entity.isEntityAlive();

        if (ModuleManager.antiBots.isEnabled() && AntiBots.isBot(entity))
            return false;

        if (FriendManager.isFriend(entity))
            return false;

        return valid;
    }

    public void sortTargets() {
        BowAimBot.attackList.sort(Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e)));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BowAimBot.attackList.clear();
        BowAimBot.currentTarget = 0;
    }

    private float getLaunchAngle(EntityLivingBase targetEntity, double v, double g) {
        double yDif = targetEntity.posY + targetEntity.getEyeHeight() / 2.0F - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double xDif = targetEntity.posX - mc.thePlayer.posX;
        double zDif = targetEntity.posZ - mc.thePlayer.posZ;

        double xCoord = Math.sqrt(xDif * xDif + zDif * zDif);

        return theta(v + 2, g, xCoord, yDif);
    }

    private float theta(double v, double g, double x, double y) {
        double yv = 2.0D * y * (v * v);
        double gx = g * (x * x);
        double g2 = g * (gx + yv);
        double insqrt = v * v * v * v - g2;
        double sqrt = Math.sqrt(insqrt);

        double numerator = v * v + sqrt;
        double numerator2 = v * v - sqrt;

        double atan1 = Math.atan2(numerator, g * x);
        double atan2 = Math.atan2(numerator2, g * x);

        return (float) Math.min(atan1, atan2);
    }
}
