package tech.imxianyu.module.impl.movement.phase;

import net.minecraft.block.BlockAir;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.impl.movement.Phase;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.world.BlockUtils;

/**
 * @author ImXianyu
 * @since 1/8/2023 11:49 AM
 */
public class Vanilla extends SubModule<Phase> {

    public Vanilla() {
        super("Vanilla");
    }
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        boolean isInsideBlock = BlockUtils.collideBlockIntersects(mc.thePlayer.getEntityBoundingBox(), block -> !(block instanceof BlockAir));

        if(isInsideBlock) {
            mc.thePlayer.noClip = true;
            mc.thePlayer.motionY = 0D;
            mc.thePlayer.onGround = true;
        }

        if(!mc.thePlayer.onGround || !this.getModule().tickTimer.isDelayed(2) || !mc.thePlayer.isCollidedHorizontally || !(!isInsideBlock || mc.thePlayer.isSneaking()))
            return;

        NetHandlerPlayClient netHandlerPlayClient = mc.getNetHandler();
        netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
        netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(0.5D, 0, 0.5D, true));
        netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
        netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.2D, mc.thePlayer.posZ, true));
        netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(0.5D, 0, 0.5D, true));
        netHandlerPlayClient.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 0.5D, mc.thePlayer.posY, mc.thePlayer.posZ + 0.5D, true));
        final double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
        final double x = -Math.sin(yaw) * 0.04D;
        final double z = Math.cos(yaw) * 0.04D;
        mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
        this.getModule().tickTimer.reset();
    };

}
