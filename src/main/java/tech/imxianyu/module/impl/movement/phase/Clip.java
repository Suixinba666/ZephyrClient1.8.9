package tech.imxianyu.module.impl.movement.phase;

import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.impl.movement.Phase;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.world.BlockUtils;

/**
 * @author ImXianyu
 * @since 1/8/2023 11:58 AM
 */
public class Clip extends SubModule<Phase> {

    public Clip() {
        super("Clip");
    }
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        boolean isInsideBlock = BlockUtils.collideBlockIntersects(mc.thePlayer.getEntityBoundingBox(), block -> !(block instanceof BlockAir));

        if (isInsideBlock) {
            mc.thePlayer.noClip = true;
            mc.thePlayer.motionY = 0D;
            mc.thePlayer.onGround = true;
        }

        if(!this.getModule().tickTimer.isDelayed(2) || !mc.thePlayer.isCollidedHorizontally || !(!isInsideBlock || mc.thePlayer.isSneaking()))
            return;

        final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
        final double oldX = mc.thePlayer.posX;
        final double oldZ = mc.thePlayer.posZ;

        for(int i = 1; i <= 10; i++) {
            final double x = -Math.sin(yaw) * i;
            final double z = Math.cos(yaw) * i;

            if(mc.theWorld.getBlockState(new BlockPos(oldX + x, mc.thePlayer.posY, oldZ + z)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(oldX + x, mc.thePlayer.posY + 1, oldZ + z)).getBlock() instanceof BlockAir) {
                mc.thePlayer.setPosition(oldX + x, mc.thePlayer.posY, oldZ + z);
                break;
            }
        }
        this.getModule().tickTimer.reset();
    };
}
