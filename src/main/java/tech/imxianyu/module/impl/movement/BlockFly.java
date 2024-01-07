package tech.imxianyu.module.impl.movement;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.*;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.events.rendering.RenderPlayerRotationsEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.widget.impl.keystrokes.CPSUtils;

import java.util.Random;

/**
 * @author ImXianyu
 * @since 6/19/2023 8:17 PM
 */
public class BlockFly extends Module {

    public BooleanSetting sprint = new BooleanSetting("Sprint", false);
    public NumberSetting<Double> range = new NumberSetting<>("Search Range", 4.0, 1.0, 6.0, 0.1);
    public ModeSetting<RotationMode> mode = new ModeSetting<>("Rotation Mode", RotationMode.LockView);
    public BooleanSetting keepY = new BooleanSetting("Keep Y", false);
    public BooleanSetting tower = new BooleanSetting("Tower", false);
    public BooleanSetting towerMove = new BooleanSetting("Tower Move", false, () -> tower.getValue());
    public BooleanSetting eagle = new BooleanSetting("Eagle", false);
    public BlockPos targetPos = BlockPos.ORIGIN;
    @Handler
    public void onRender3D(Render3DEvent event) {
        RenderSystem.drawBlockBox(targetPos, -1, false);
    }
    public int lastSlot = -1;
    public float[] rot = new float[2];
    @Handler
    public void onRotation(RenderPlayerRotationsEvent event) {
        if (this.targetPos != BlockPos.ORIGIN) {
            event.rotationYaw = rot[0];
            event.rotationPitch = rot[1];
        }
    };
    public float[] targetRot = new float[2];
    int startY;
    boolean canPlace = false;
    BlockData blockData;
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (mc.theWorld == null || !event.isPre()) {
            return;
        }

        boolean hasBlockInHotBar = false;

        for (int i = 0; i < 9; ++i) {
            ItemStack item1 = mc.thePlayer.inventory.getStackInSlot(i);
            if (item1 != null) {
                if (item1.getItem() instanceof ItemBlock) {
                    hasBlockInHotBar = true;
                    mc.thePlayer.inventory.currentItem = i;
                    break;
                }
            }
        }

        if (hasBlockInHotBar) {
            this.blockData = this.getBlockData(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0,
                    mc.thePlayer.posZ)) == null
                    ? this.getBlockData(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0,
                    mc.thePlayer.posZ).down(1))
                    : this.getBlockData(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0,
                    mc.thePlayer.posZ));
            if (this.blockData == null)
                return;

            targetPos = this.blockData.pos;


            if (eagle.getValue()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)).getBlock().getMaterial() == Material.air);
            }

            double[] temp = getPosForRotation(this.blockData.pos, this.blockData.facing);
            double pX = temp[0];
            double pY = temp[1] + 0.5;
            double pZ = temp[2];
            targetRot = getRotations(pX, pY, pZ, this.blockData.facing);
            rot[0] = AnimationSystem.interpolate(rot[0], rot[0] + (this.blockData.facing == EnumFacing.UP ? 0 : MathHelper.wrapAngleTo180_float(getYaw(this.blockData.facing)[0] - rot[0]))/* + generateRandomFloat(6)*/, 0.6f);
            rot[1] = AnimationSystem.interpolate(rot[1], (this.blockData.facing == EnumFacing.UP ? targetRot[1] : 82.5f)/* + generateRandomFloat(1)*/, 0.6f);
            if (mode.getValue() == RotationMode.Silent) {
                event.setRotationYaw(rot[0]);
                event.setRotationPitch(rot[1]);
            } else {
                mc.thePlayer.rotationYaw = rot[0];
                mc.thePlayer.rotationPitch = rot[1];
            }
            canPlace = true;


            double x = mc.thePlayer.posX;
            double y = mc.thePlayer.posY - 1.0;
            double z = mc.thePlayer.posZ;
            BlockPos underPos = new BlockPos(x, y, z);
            final Block underBlock = mc.theWorld.getBlockState(underPos).getBlock();
            if (this.tower.getValue() && mc.gameSettings.keyBindJump.pressed) {
                if (this.isAirBlock(underBlock) && this.blockData != null) {
//                    mc.thePlayer.motionY = 0.4196;
                    mc.thePlayer.jump();
                    mc.thePlayer.motionX *= 0.5;
                    mc.thePlayer.motionZ *= 0.5;
                }

            }
        }
    };

    @Handler
    public void onMove(MoveEvent event) {
        if (mc.gameSettings.keyBindJump.pressed && this.tower.getValue() && !this.towerMove.getValue()) {
            event.x = 0;
            event.z = 0;
        }
    };

    public boolean isAirBlock(final Block block) {
        return block.getMaterial().isReplaceable() && (!(block instanceof BlockSnow) || block.getBlockBoundsMaxY() <= 0.125);
    }


    @Handler
    public void onTick(TickEvent event) {
        if (mc.theWorld == null || event.isPre())
            return;

        if (blockData == null)
            return;

        if (canPlace && canPlace(this.targetRot[0], this.targetRot[1], this.blockData.facing) && (PlayerUtils.isMoving() || PlayerUtils.isMoving2())) {
            canPlace = false;
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), targetPos, blockData.facing,
                    blockDataToVec3(blockData.pos, blockData.facing))) {
                mc.thePlayer.swingItem();
            }

            CPSUtils.addRightCPS();
        }
    };
    Random rand = new Random();

    public BlockFly() {
        super("Block Fly", Category.MOVEMENT);
    }

    public static float[] getRotations(double posX, double posY, double posZ, EnumFacing facing) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        BlockPos blockPos = new BlockPos(player.posX, player.posY, player.posZ);
        double x = posX - player.posX;
        double y = posY - (player.posY + (double) player.getEyeHeight());
        double z = posZ - player.posZ;

        if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
            x = posX - (blockPos.getX() + 0.5);
        }

        if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
            z = posZ - (blockPos.getZ() + 0.5);
        }

        double dist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            lastSlot = mc.thePlayer.inventory.currentItem;
            rot[0] = mc.thePlayer.rotationYaw;
            rot[1] = mc.thePlayer.rotationPitch;
            mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            startY = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ).getY();
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            if (lastSlot != -1) {
                mc.thePlayer.inventory.currentItem = lastSlot;
                lastSlot = -1;
            }
            mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));

        }

        targetPos = BlockPos.ORIGIN;
    }

    /**
     * @param bound bound
     * @return A float that is between negative bound to positive bound.
     */
    public float generateRandomFloat(float bound) {
        return (rand.nextInt((int) (bound * 1000)) / 1000.0f) * (rand.nextBoolean() ? -1 : 1);
    }

    public boolean isOnGround(final double n) {
        return !this.mc.theWorld.getCollidingBoundingBoxes(this.mc.thePlayer, this.mc.thePlayer.getEntityBoundingBox().offset(0.0, -n, 0.0)).isEmpty();
    }

    public double[] getPosForRotation(BlockPos blockPos, EnumFacing facing) {
        double[] result = new double[3];

        result[0] = blockPos.getX() + 0.5;
        result[1] = blockPos.getY() + 0.5;
        result[2] = blockPos.getZ() + 0.5;

        switch (facing) {
            case UP: {
                result[1] += 0.5;
                break;
            }

            case SOUTH: {
                result[2] += 0.5;
                break;

            }

            case NORTH: {
                result[2] -= 0.5;
                break;

            }

            case EAST: {
                result[0] += 0.5;
                break;

            }

            case WEST: {
                result[0] -= 0.5;
                break;

            }

        }

        if (facing != EnumFacing.DOWN) {
            result[1] -= 0.1;
        }

        return result;
    }


    public boolean canPlace(float yaw, float pitch, EnumFacing sideHit) {

        float wasYaw = this.mc.getRenderViewEntity().rotationYaw;
        float wasPitch = this.mc.getRenderViewEntity().rotationPitch;

        this.mc.getRenderViewEntity().rotationYaw = yaw;
        this.mc.getRenderViewEntity().rotationPitch = pitch;

        double d0 = this.mc.playerController.getBlockReachDistance();
        MovingObjectPosition movingObjectPosition = this.mc.getRenderViewEntity().rayTrace(d0, 1);


        this.mc.getRenderViewEntity().rotationPitch = wasPitch;
        this.mc.getRenderViewEntity().rotationYaw = wasYaw;

//        mc.thePlayer.addChatMessage(new ChatComponentText("Expected: " + sideHit + ", Actual: " + movingObjectPosition.sideHit));

        return movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && movingObjectPosition.sideHit == sideHit;
    }

    public float[] getYaw(EnumFacing facing) {


        switch (facing) {
            case UP -> {
                return new float[]{0.0f, 90.0f};
            }
            case NORTH -> {
                return new float[]{0.0f, 82.0f};
            }
            case WEST -> {
                return new float[]{-90.0f, 82.0f};
            }
            case SOUTH -> {
                return new float[]{180.0f, 82.0f};
            }
            case EAST -> {
                return new float[]{90.0f, 82.0f};
            }
        }

        return new float[]{0.0f, 82.0f};
    }

    private Vec3 blockDataToVec3(BlockPos paramBlockPos, EnumFacing paramEnumFacing) {
        double d1 = paramBlockPos.getX() + 0.5D;
        double d2 = paramBlockPos.getY() + 0.5D;
        double d3 = paramBlockPos.getZ() + 0.5D;
        d1 += paramEnumFacing.getFrontOffsetX() / 2.0D;
        d3 += paramEnumFacing.getFrontOffsetZ() / 2.0D;
        d2 += paramEnumFacing.getFrontOffsetY() / 2.0D;
        return new Vec3(d1, d2, d3);
    }

    private BlockData getBlockData(BlockPos paramBlockPos) {
        if (isValidBlock(paramBlockPos.add(0, -1, 0))) {
            return new BlockData(paramBlockPos.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(paramBlockPos.add(-1, 0, 0))) {
            return new BlockData(paramBlockPos.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(paramBlockPos.add(1, 0, 0))) {
            return new BlockData(paramBlockPos.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(paramBlockPos.add(0, 0, 1))) {
            return new BlockData(paramBlockPos.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(paramBlockPos.add(0, 0, -1))) {
            return new BlockData(paramBlockPos.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos localBlockPos1 = paramBlockPos.add(-1, 0, 0);
        if (isValidBlock(localBlockPos1.add(0, -1, 0))) {
            return new BlockData(localBlockPos1.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(localBlockPos1.add(-1, 0, 0))) {
            return new BlockData(localBlockPos1.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(localBlockPos1.add(1, 0, 0))) {
            return new BlockData(localBlockPos1.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(localBlockPos1.add(0, 0, 1))) {
            return new BlockData(localBlockPos1.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(localBlockPos1.add(0, 0, -1))) {
            return new BlockData(localBlockPos1.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos localBlockPos2 = paramBlockPos.add(1, 0, 0);
        if (isValidBlock(localBlockPos2.add(0, -1, 0))) {
            return new BlockData(localBlockPos2.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(localBlockPos2.add(-1, 0, 0))) {
            return new BlockData(localBlockPos2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(localBlockPos2.add(1, 0, 0))) {
            return new BlockData(localBlockPos2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(localBlockPos2.add(0, 0, 1))) {
            return new BlockData(localBlockPos2.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(localBlockPos2.add(0, 0, -1))) {
            return new BlockData(localBlockPos2.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos localBlockPos3 = paramBlockPos.add(0, 0, 1);
        if (isValidBlock(localBlockPos3.add(0, -1, 0))) {
            return new BlockData(localBlockPos3.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(localBlockPos3.add(-1, 0, 0))) {
            return new BlockData(localBlockPos3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(localBlockPos3.add(1, 0, 0))) {
            return new BlockData(localBlockPos3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(localBlockPos3.add(0, 0, 1))) {
            return new BlockData(localBlockPos3.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(localBlockPos3.add(0, 0, -1))) {
            return new BlockData(localBlockPos3.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos localBlockPos4 = paramBlockPos.add(0, 0, -1);
        if (isValidBlock(localBlockPos4.add(0, -1, 0))) {
            return new BlockData(localBlockPos4.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(localBlockPos4.add(-1, 0, 0))) {
            return new BlockData(localBlockPos4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(localBlockPos4.add(1, 0, 0))) {
            return new BlockData(localBlockPos4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(localBlockPos4.add(0, 0, 1))) {
            return new BlockData(localBlockPos4.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(localBlockPos4.add(0, 0, -1))) {
            return new BlockData(localBlockPos4.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos localBlockPos5 = paramBlockPos.add(0, -1, 0);
        if (isValidBlock(localBlockPos5.add(0, -1, 0))) {
            return new BlockData(localBlockPos5.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(localBlockPos5.add(-1, 0, 0))) {
            return new BlockData(localBlockPos5.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(localBlockPos5.add(1, 0, 0))) {
            return new BlockData(localBlockPos5.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(localBlockPos5.add(0, 0, 1))) {
            return new BlockData(localBlockPos5.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(localBlockPos5.add(0, 0, -1))) {
            return new BlockData(localBlockPos5.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos localBlockPos6 = localBlockPos5.add(1, 0, 0);
        if (isValidBlock(localBlockPos6.add(0, -1, 0))) {
            return new BlockData(localBlockPos6.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(localBlockPos6.add(-1, 0, 0))) {
            return new BlockData(localBlockPos6.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(localBlockPos6.add(1, 0, 0))) {
            return new BlockData(localBlockPos6.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(localBlockPos6.add(0, 0, 1))) {
            return new BlockData(localBlockPos6.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(localBlockPos6.add(0, 0, -1))) {
            return new BlockData(localBlockPos6.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos localBlockPos7 = localBlockPos5.add(-1, 0, 0);
        if (isValidBlock(localBlockPos7.add(0, -1, 0))) {
            return new BlockData(localBlockPos7.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(localBlockPos7.add(-1, 0, 0))) {
            return new BlockData(localBlockPos7.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(localBlockPos7.add(1, 0, 0))) {
            return new BlockData(localBlockPos7.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(localBlockPos7.add(0, 0, 1))) {
            return new BlockData(localBlockPos7.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(localBlockPos7.add(0, 0, -1))) {
            return new BlockData(localBlockPos7.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos localBlockPos8 = localBlockPos5.add(0, 0, 1);
        if (isValidBlock(localBlockPos8.add(0, -1, 0))) {
            return new BlockData(localBlockPos8.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(localBlockPos8.add(-1, 0, 0))) {
            return new BlockData(localBlockPos8.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(localBlockPos8.add(1, 0, 0))) {
            return new BlockData(localBlockPos8.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(localBlockPos8.add(0, 0, 1))) {
            return new BlockData(localBlockPos8.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(localBlockPos8.add(0, 0, -1))) {
            return new BlockData(localBlockPos8.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos localBlockPos9 = localBlockPos5.add(0, 0, -1);
        if (isValidBlock(localBlockPos9.add(0, -1, 0))) {
            return new BlockData(localBlockPos9.add(0, -1, 0), EnumFacing.UP);
        }
        if (isValidBlock(localBlockPos9.add(-1, 0, 0))) {
            return new BlockData(localBlockPos9.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isValidBlock(localBlockPos9.add(1, 0, 0))) {
            return new BlockData(localBlockPos9.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isValidBlock(localBlockPos9.add(0, 0, 1))) {
            return new BlockData(localBlockPos9.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isValidBlock(localBlockPos9.add(0, 0, -1))) {
            return new BlockData(localBlockPos9.add(0, 0, -1), EnumFacing.SOUTH);
        }
        return null;
    }

    private boolean isValidBlock(BlockPos paramBlockPos) {
        Block localBlock = mc.theWorld.getBlockState(paramBlockPos).getBlock();

        if (keepY.getValue() && paramBlockPos.getY() != startY)
            return false;

        if ((localBlock.getMaterial().isSolid()) || (!localBlock.isTranslucent()) || (localBlock.isSolidFullCube()) || ((localBlock instanceof BlockLadder)) || ((localBlock instanceof BlockCarpet)) || ((localBlock instanceof BlockSnow)) || ((localBlock instanceof BlockSkull))) {
            return !localBlock.getMaterial().isLiquid();
        }
        return false;
    }


    public enum RotationMode {
        Silent,
        LockView
    }

    public static class BlockData {
        public BlockPos pos;
        public EnumFacing facing;

        private BlockData(BlockPos paramBlockPos, EnumFacing paramEnumFacing) {
            this.pos = paramBlockPos;
            this.facing = paramEnumFacing;
        }
    }


}
