package tech.imxianyu.module.impl.movement.velocity;

import net.minecraft.network.play.server.S12PacketEntityVelocity;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.ReceivePacketEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.impl.movement.Velocity;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.NumberSetting;

/**
 * @author ImXianyu
 * @since 6/24/2023 11:40 AM
 */
public class Simple extends SubModule<Velocity> {

    public Simple() {
        super("Simple");
    }

    public NumberSetting<Integer> x = new NumberSetting<Integer>("X", 100, 0, 100, 1) {
        @Override
        public String getStringForRender() {
            return this.getValue() + "%";
        }
    };
    public NumberSetting<Integer> y = new NumberSetting<Integer>("Y", 100, 0, 100, 1) {
        @Override
        public String getStringForRender() {
            return this.getValue() + "%";
        }
    };

    @Handler
    public void onTick(TickEvent event) {
        this.getModule().setSuffix(x.getStringForRender() + ", " + y.getStringForRender());
    };

    @Handler
    public void onReceive(ReceivePacketEvent event) {

        if (event.getPacket() instanceof S12PacketEntityVelocity) {

            if (mc.thePlayer == null || mc.theWorld.getEntityByID(((S12PacketEntityVelocity) event.getPacket()).getEntityID()) != mc.thePlayer) {
                return;
            }

            S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();

            packet.motionX = (packet.getMotionX() * (x.getValue() / 100));
            packet.motionY = (packet.getMotionY() * (y.getValue() / 100));
            packet.motionZ = (packet.getMotionZ() * (x.getValue() / 100));
        }
    };

}
