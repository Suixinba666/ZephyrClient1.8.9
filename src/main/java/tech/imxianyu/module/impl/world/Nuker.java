package tech.imxianyu.module.impl.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockTNT;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import tech.imxianyu.eventapi.EnumPriority;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.eventapi.Priority;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.rendering.RenderPlayerRotationsEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.rotation.RotationUtils;
import tech.imxianyu.utils.timing.Timer;

public class Nuker extends Module {

    public Nuker() {
        super("Nuker", Category.WORLD);
    }

    public NumberSetting<Double> radius = new NumberSetting<>("Radius", 3.0, 1.0, 10.0, 0.1);
    public BooleanSetting silent = new BooleanSetting("Silent", true);

    int posX, posY, posZ;
    private Timer timer = new Timer();
    boolean canBreak = false;


    @Handler
    public void onPreUpdate(UpdateEvent e) {
        if (!e.isPre())
            return;

        int radius = this.radius.getValue().intValue();
        for(int y = radius; y >= -radius; --y) {
            for(int x = -radius; x < radius; ++x) {
                for(int z = -radius; z < radius; ++z) {
                    this.posX = (int)Math.floor(this.mc.thePlayer.posX) + x;
                    this.posY = (int)Math.floor(this.mc.thePlayer.posY) + y;
                    this.posZ = (int)Math.floor(this.mc.thePlayer.posZ) + z;
                    if(this.mc.thePlayer.getDistanceSq(this.mc.thePlayer.posX + (double)x, this.mc.thePlayer.posY + (double)y, this.mc.thePlayer.posZ + (double)z) <= 16.0D) {
                        Block block = mc.theWorld.getBlockState(new BlockPos(this.posX , this.posY, this.posZ)).getBlock();
                        boolean blockChecks = timer.isDelayed(100L);

//                        blockChecks = blockChecks && mc.thePlayer.canPosBeSeen(this.posX + 0.5F, this.posY + 0.9f, this.posZ + 0.5F);
//                        blockChecks = blockChecks && (block.getBlockHardness(this.mc.theWorld, BlockPos.ORIGIN) != -1.0F || this.mc.playerController.isInCreativeMode());
                        if(blockChecks) {
//                            System.out.println(block);

                            this.canBreak = true;

                            float[] angles = RotationUtils.getRotations(this.posX + 0.5F, this.posY + 0.9, this.posZ + 0.5F);
                            if(silent.getValue()){

                                e.setRotationYaw(angles[0]);
                                e.setRotationPitch(angles[1]);
                            } else {
                                mc.thePlayer.rotationYaw = angles[0];
                                mc.thePlayer.rotationPitch = angles[1];
                            }
                            return;
                        }
                    }
                }
            }
        }
    };

    @Priority(priority = EnumPriority.LOWEST)
    @Handler
    public void onRotation(RenderPlayerRotationsEvent event) {
        float[] rotations = RotationUtils.getRotations(this.posX, this.posY, this.posZ);
        event.setRotationYaw(rotations[0]);
        event.setRotationPitch(rotations[1]);
    };

    @Handler
    public void onPostUpdate(UpdateEvent e) {
        if(this.canBreak) {
            this.mc.thePlayer.swingItem();
            this.mc.playerController.clickBlock(new BlockPos(this.posX , this.posY, this.posZ), getFacing(new BlockPos(this.posX , this.posY, this.posZ)));
            if((double)this.mc.playerController.getCurBlockDamageMP() >= 1.0D)
                timer.reset();
            canBreak = false;
        }
    };

    public EnumFacing getFacing(BlockPos pos) {
        EnumFacing[] orderedValues = new EnumFacing[]{EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.DOWN};
        EnumFacing[] var2 = orderedValues;
        int var3 = orderedValues.length;
        for(int var4 = 0; var4 < var3; ++var4) {
            EnumFacing facing = var2[var4];
            EntitySnowball temp = new EntitySnowball(mc.theWorld);
            temp.posX = (double)pos.getX() + 0.5D;
            temp.posY = (double)pos.getY() + 0.5D;
            temp.posZ = (double)pos.getZ() + 0.5D;
            temp.posX += (double)facing.getDirectionVec().getX() * 0.5D;
            temp.posY += (double)facing.getDirectionVec().getY() * 0.5D;
            temp.posZ += (double)facing.getDirectionVec().getZ() * 0.5D;
            if(mc.thePlayer.canEntityBeSeen(temp)) {
                return facing;
            }
        }

        return null;
    }

}
