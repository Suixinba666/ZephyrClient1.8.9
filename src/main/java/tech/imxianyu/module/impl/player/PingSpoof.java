package tech.imxianyu.module.impl.player;

import net.minecraft.network.Packet;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.NumberSetting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ImXianyu
 * @since 6/25/2023 10:48 PM
 */
public class PingSpoof extends Module {

    final Map<Packet<?>, Long> packetQueue = new HashMap<>();
    @Handler
    public void onUpdate(UpdateEvent event) {
        try {
            synchronized (packetQueue) {

                for (Iterator<Map.Entry<Packet<?>, Long>> iterator = packetQueue.entrySet().iterator(); iterator.hasNext(); ) {
                    final Map.Entry<Packet<?>, Long> entry = iterator.next();
                    if (entry.getValue() < System.currentTimeMillis()) {
                        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(entry.getKey());
                        iterator.remove();
                    }
                }
            }
        } catch (Exception ignored) {

        }
    };
    public NumberSetting<Integer> delay = new NumberSetting<Integer>("Delay", 0, 0, 2000, 1) {
        @Override
        public String getStringForRender() {
            return this.getValue() + "ms";
        }
    };
    @Handler
    public void onReceive(SendPacketEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null)
            return;

        event.setCancelled();
        synchronized (packetQueue) {
            packetQueue.put(event.getPacket(), System.currentTimeMillis() + delay.getValue());
        }
    };


    public PingSpoof() {
        super("Ping Spoof", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        synchronized (packetQueue) {
            for (Map.Entry<Packet<?>, Long> entry : packetQueue.entrySet()) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(entry.getKey());
            }

            packetQueue.clear();
        }
    }
}
