package tech.imxianyu.utils.world;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

/**
 * @author ImXianyu
 * @since 1/8/2023 11:51 AM
 */
public class BlockUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean collideBlockIntersects(AxisAlignedBB axisAlignedBB, Collidable collidable) {
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                BlockPos blockPos = new BlockPos(x, axisAlignedBB.minY, z);
                Block block = mc.theWorld.getBlockState(blockPos).getBlock();

                if (collidable.collideBlock(block)) {
                    AxisAlignedBB boundingBox = block.getCollisionBoundingBox(mc.theWorld, blockPos, mc.theWorld.getBlockState(blockPos));

                    if (boundingBox == null)
                        continue;

                    if (mc.thePlayer.getEntityBoundingBox().intersectsWith(boundingBox))
                        return true;
                }
            }
        }

        return false;
    }

    public interface Collidable {

        /**
         * Check if [block] is collidable
         */
        boolean collideBlock(Block block);
    }
}
