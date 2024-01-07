package tech.imxianyu.utils.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ImXianyu
 * @since 2022/7/20 10:24
 */
public class PlayerUtils {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static boolean isMoving2() {
        return ((mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F));
    }

    public static boolean isMoving() {
        for (KeyBinding k : new KeyBinding[]{mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump}) {
            if (k.pressed) {
                return true;
            }
        }

        return false;
    }

    public static double fovFromEntity(Entity en) {
        return ((double) (mc.thePlayer.rotationYaw - fovToEntity(en)) % 360.0D + 540.0D) % 360.0D - 180.0D;
    }

    public static float fovToEntity(Entity ent) {
        double x = ent.posX - mc.thePlayer.posX;
        double z = ent.posZ - mc.thePlayer.posZ;
        double yaw = Math.atan2(x, z) * 57.2957795D;
        return (float) (yaw * -1.0D);
    }

    public static Entity getEntity(double distance) {
        if (getEntity(distance, 0.0) == null) {
            return null;
        }
        return (getEntity(distance, 0.0));
    }

    public static Entity getEntity(double distance, double expand) {
        Entity var2 = mc.getRenderViewEntity();
        Entity entity = null;
        if (var2 == null || mc.theWorld == null) {
            return null;
        }
        mc.mcProfiler.startSection("pick");
        Vec3 eyePos = var2.getPositionEyes(0.0f);
        Vec3 var4 = var2.getLook(0.0f);
        Vec3 var5 = eyePos.addVector(var4.xCoord * distance, var4.yCoord * distance, var4.zCoord * distance);
        Vec3 var6 = null;
        List<Entity> var8 = mc.theWorld.getEntitiesWithinAABBExcludingEntity(var2, var2.getEntityBoundingBox().addCoord(var4.xCoord * distance, var4.yCoord * distance, var4.zCoord * distance).expand(1.0, 1.0, 1.0));
        double var9 = distance;
        int var10 = 0;
        while (var10 < var8.size()) {
            Entity var11 = var8.get(var10);
            if (var11.canBeCollidedWith()) {
                double var15;
                float var12 = var11.getCollisionBorderSize();
                AxisAlignedBB var13 = var11.getEntityBoundingBox().expand(var12, var12, var12);
                var13 = var13.expand(expand, expand, expand);
                MovingObjectPosition var14 = var13.calculateIntercept(eyePos, var5);
                if (var13.isVecInside(eyePos)) {
                    if (0.0 < var9 || var9 == 0.0) {
                        entity = var11;
                        var6 = var14 == null ? eyePos : var14.hitVec;
                        var9 = 0.0;
                    }
                } else if (var14 != null && ((var15 = eyePos.distanceTo(var14.hitVec)) < var9 || var9 == 0.0)) {
                    boolean canRiderInteract = false;
                    if (var11 == var2.ridingEntity && !canRiderInteract) {
                        if (var9 == 0.0) {
                            entity = var11;
                            var6 = var14.hitVec;
                        }
                    } else {
                        entity = var11;
                        var6 = var14.hitVec;
                        var9 = var15;
                    }
                }
            }
            ++var10;
        }
        return _getEntity(entity, distance, var6, var9, mc);
    }


    public static List<Entity> getEntities(double distance, double expand) {
        Entity var2 = mc.getRenderViewEntity();
        if (var2 == null || mc.theWorld == null) {
            return null;
        }
        mc.mcProfiler.startSection("pick");
        Vec3 eyePos = var2.getPositionEyes(0.0f);
        Vec3 playerLook = var2.getLook(0.0f);
        Vec3 end = eyePos.addVector(playerLook.xCoord * distance, playerLook.yCoord * distance, playerLook.zCoord * distance);
        List<Entity> result = new ArrayList<>();
        List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(var2, var2.getEntityBoundingBox().addCoord(playerLook.xCoord * distance, playerLook.yCoord * distance, playerLook.zCoord * distance).expand(1.0, 1.0, 1.0));
        double reach = distance;
        for (Entity ent : entities) {
            if (ent.canBeCollidedWith()) {
                float var12 = ent.getCollisionBorderSize();
                AxisAlignedBB aabb = ent.getEntityBoundingBox().expand(var12, var12, var12);
                aabb = aabb.expand(expand, expand, expand);
                MovingObjectPosition var14 = aabb.calculateIntercept(eyePos, end);
                if (aabb.isVecInside(eyePos)) {
                    if (0.0 < reach || reach == 0.0) {
                        result.add(ent);
                    }
                } else if (var14 != null && (eyePos.distanceTo(var14.hitVec) < reach || reach == 0.0)) {
                    result.add(ent);
                }
            }
        }
        mc.mcProfiler.endSection();
        return result;
    }

    public static MovingObjectPosition rayTrace(float yaw, float pitch, double distance, float partialTicks) {
        Vec3 vec3 = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 vec4 = mc.thePlayer.getVectorForRotation(pitch, yaw);
        Vec3 vec5 = vec3.addVector(vec4.xCoord * distance, vec4.yCoord * distance, vec4.zCoord * distance);
        return mc.theWorld.rayTraceBlocks(vec3, vec5, !mc.thePlayer.isInWater(), false, false);
    }


    public static Entity getEntityWithGivenRotation(double distance, double expand, float prevRotationYaw, float prevRotationPitch, float rotationYaw, float rotationPitch) {
        Entity var2 = mc.getRenderViewEntity();
        Entity entity = null;
        if (var2 == null || mc.theWorld == null) {
            return null;
        }
        mc.mcProfiler.startSection("pick");
        Vec3 var3 = var2.getPositionEyes(0.0f);
        Vec3 var4 = var2.getLookWithGivenRotation(0.0f, prevRotationYaw, prevRotationPitch, rotationYaw, rotationPitch);
        Vec3 var5 = var3.addVector(var4.xCoord * distance, var4.yCoord * distance, var4.zCoord * distance);
        Vec3 var6 = null;
        List<Entity> var8 = mc.theWorld.getEntitiesWithinAABBExcludingEntity(var2, var2.getEntityBoundingBox().addCoord(var4.xCoord * distance, var4.yCoord * distance, var4.zCoord * distance).expand(1.0, 1.0, 1.0));
        double var9 = distance;
        int var10 = 0;
        while (var10 < var8.size()) {
            Entity var11 = var8.get(var10);
            if (var11.canBeCollidedWith()) {
                double var15;
                float var12 = var11.getCollisionBorderSize();
                AxisAlignedBB var13 = var11.getEntityBoundingBox().expand(var12, var12, var12);
                var13 = var13.expand(expand, expand, expand);
                MovingObjectPosition var14 = var13.calculateIntercept(var3, var5);
                if (var13.isVecInside(var3)) {
                    if (0.0 < var9 || var9 == 0.0) {
                        entity = var11;
                        var6 = var14 == null ? var3 : var14.hitVec;
                        var9 = 0.0;
                    }
                } else if (var14 != null && ((var15 = var3.distanceTo(var14.hitVec)) < var9 || var9 == 0.0)) {
                    boolean canRiderInteract = false;
                    if (var11 == var2.ridingEntity && !canRiderInteract) {
                        if (var9 == 0.0) {
                            entity = var11;
                            var6 = var14.hitVec;
                        }
                    } else {
                        entity = var11;
                        var6 = var14.hitVec;
                        var9 = var15;
                    }
                }
            }
            ++var10;
        }
        return _getEntity(entity, distance, var6, var9, mc);
    }

    public static Vec3 getLook(float partialTicks, float rotationYaw, float rotationPitch, float prevRotationYaw, float prevRotationPitch) {
        float f = prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks;
        float f1 = prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTicks;
        return getVectorForRotation(f, f1);
    }

    protected static Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public static Entity getEntity(double distance, double expand, float yaw, float pitch, float prevYaw, float prevPitch, float partialTicks) {
        Entity var2 = mc.getRenderViewEntity();
        Entity entity = null;
        if (var2 == null || mc.theWorld == null) {
            return null;
        }
        mc.mcProfiler.startSection("pick");
        Vec3 var3 = var2.getPositionEyes(0.0f);
        Vec3 var4 = getLook(0.0f, yaw, pitch, prevYaw, prevPitch);
        Vec3 var5 = var3.addVector(var4.xCoord * distance, var4.yCoord * distance, var4.zCoord * distance);
        Vec3 var6 = null;
        List<Entity> var8 = mc.theWorld.getEntitiesWithinAABBExcludingEntity(var2, var2.getEntityBoundingBox().addCoord(var4.xCoord * distance, var4.yCoord * distance, var4.zCoord * distance).expand(1.0, 1.0, 1.0));
        double var9 = distance;
        int var10 = 0;
        while (var10 < var8.size()) {
            Entity var11 = var8.get(var10);
            if (var11.canBeCollidedWith()) {
                double var15;
                float var12 = var11.getCollisionBorderSize();
                AxisAlignedBB var13 = var11.getEntityBoundingBox().expand(var12, var12, var12);
                var13 = var13.expand(expand, expand, expand);
                MovingObjectPosition var14 = var13.calculateIntercept(var3, var5);
                if (var13.isVecInside(var3)) {
                    if (0.0 < var9 || var9 == 0.0) {
                        entity = var11;
                        var6 = var14 == null ? var3 : var14.hitVec;
                        var9 = 0.0;
                    }
                } else if (var14 != null && ((var15 = var3.distanceTo(var14.hitVec)) < var9 || var9 == 0.0)) {
                    boolean canRiderInteract = false;
                    if (var11 == var2.ridingEntity && !canRiderInteract) {
                        if (var9 == 0.0) {
                            entity = var11;
                            var6 = var14.hitVec;
                        }
                    } else {
                        entity = var11;
                        var6 = var14.hitVec;
                        var9 = var15;
                    }
                }
            }
            ++var10;
        }
        return _getEntity(entity, distance, var6, var9, mc);
    }

    public static Entity _getEntity(Entity entity, double var5, Vec3 var10, double var13, Minecraft mc) {
        if (var13 < var5 && !(entity instanceof EntityLivingBase) && !(entity instanceof EntityItemFrame)) {
            entity = null;
        }
        mc.mcProfiler.endSection();
        if (entity == null || var10 == null) {
            return null;
        }
        return entity;
    }

    private static boolean isNumber(char c) {
        return "0123456789".contains(Character.toString(c));
    }

    private static int getRealNote(int noteIn) {
        switch (noteIn) {
            case 1: {
                return 0;
            }

            case 2: {
                return 2;
            }

            case 3: {
                return 4;
            }

            case 4: {
                return 5;
            }

            case 5: {
                return 7;
            }

            case 6: {
                return 9;
            }

            case 7: {
                return 11;
            }
        }

        return 0;
    }

    public static void playNotes(String notes) {
        //135!1
        int amplifier = 0;
        for (char c : notes.toCharArray()) {

            if (isNumber(c)) {
                playNote(amplifier * 12 + getRealNote(Integer.parseInt(Character.toString(c))));
                amplifier = 0;
            } else {
                amplifier++;
            }
        }
    }

    public static void playNote(int note) {
        float f = (float) Math.pow(2.0D, (double) (note - 12) / 12.0D);
        synchronized (mc.getSoundHandler().sndManager.playingSounds) {
            if (mc.thePlayer != null && mc.theWorld != null)
                Minecraft.getMinecraft().thePlayer.playSound("note.harp", 10.0F, f);
        }
    }
}
