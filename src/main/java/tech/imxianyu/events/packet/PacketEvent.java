package tech.imxianyu.events.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;
import tech.imxianyu.eventapi.EventCancellable;

/**
 * @author ImXianyu
 * @since 2022/7/20 9:15
 */
@AllArgsConstructor
public class PacketEvent extends EventCancellable {
    @Getter
    @Setter
    private Packet<?> packet;
}
