package tech.imxianyu.module.impl.movement.phase;

import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.impl.movement.Phase;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.world.BlockUtils;

/**
 * @author ImXianyu
 * @since 1/8/2023 11:57 AM
 */
public class Skip extends SubModule<Phase> {

    public Skip() {
        super("Skip");
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

        if(!mc.thePlayer.onGround || !this.getModule().tickTimer.isDelayed(2) || !mc.thePlayer.isCollidedHorizontally || !(!isInsideBlock || mc.thePlayer.isSneaking()))
            return;

        final double direction = mc.thePlayer.getDirection();
        final double posX = -Math.sin(direction) * 0.3;
        final double posZ = Math.cos(direction) * 0.3;

        for(int i = 0; i < 3; ++i) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.06, mc.thePlayer.posZ, true));
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + posX * i, mc.thePlayer.posY, mc.thePlayer.posZ + posZ * i, true));
        }

        mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(posX, 0.0D, posZ));
        mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX + posX, mc.thePlayer.posY, mc.thePlayer.posZ + posZ);
        this.getModule().tickTimer.reset();
    };
}
