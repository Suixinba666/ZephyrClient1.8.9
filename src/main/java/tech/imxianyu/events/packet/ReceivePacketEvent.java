package tech.imxianyu.events.packet;

import net.minecraft.network.Packet;

/**
 * @author ImXianyu
 * @since 2022/7/20 9:16
 */
public class ReceivePacketEvent extends PacketEvent {
    public ReceivePacketEvent(Packet<?> packet) {
        super(packet);
    }
}
