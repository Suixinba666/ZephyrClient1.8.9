package tech.imxianyu.module.impl.player;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.module.Module;

/**
 * @author ImXianyu
 * @since 6/28/2023 12:47 PM
 */
public class AntiHunger extends Module {

    @Handler
    public void onSend(SendPacketEvent event) {
        if (mc.thePlayer != null && mc.theWorld != null && !isOnLiquid()) {
            if ((event.getPacket() instanceof C03PacketPlayer)) {
                C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
                double yFix = mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                boolean ground = yFix == 0.0;
                if (ground && !mc.playerController.isHittingBlock)
                    packet.onGround = false;
            }
        }
    };

    public AntiHunger() {
        super("Anti Hunger", Category.PLAYER);
    }

    public boolean isOnLiquid() {
        boolean onLiquid = getBlockAtPosC(mc.thePlayer, 0.3F, 0.1F, 0.3F).getMaterial().isLiquid() &&
                getBlockAtPosC(mc.thePlayer, -0.3F, 0.1F, -0.3F).getMaterial().isLiquid();
        return onLiquid;
    }

    public Block getBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock();
    }

    public Block getBlockAtPosC(EntityPlayer inPlayer, double x, double y, double z) {
        return getBlock(new BlockPos(inPlayer.posX - x, inPlayer.posY - y, inPlayer.posZ - z));
    }

}
