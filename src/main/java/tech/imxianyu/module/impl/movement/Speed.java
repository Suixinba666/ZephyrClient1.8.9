package tech.imxianyu.module.impl.movement;

import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.ReceivePacketEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.movement.speed.*;
import tech.imxianyu.rendering.notification.Notification;
import tech.imxianyu.rendering.notification.NotificationManager;
import tech.imxianyu.settings.BooleanSetting;

/**
 * @author ImXianyu
 * @since 2022/7/20 9:32
 */
public class Speed extends Module {

    @Handler
    public void onTick(TickEvent event) {
        this.setSuffix(this.getSubModes().getValue());
    };
    public BooleanSetting lagBackCheck = new BooleanSetting("Lag Back Check", true);

    @Handler
    public void onRecv(ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && this.lagBackCheck.getValue()) {
            this.toggle();
            NotificationManager.show("Disabled " + this.getName() + " due to lag back.", Notification.Type.WARNING);
        }
    };

    public Speed() {
        super("Speed", Category.MOVEMENT);
        super.addSubModules(new AAC5(), new AutoJump(), new Strafe(), new NCPBHop(), new NCPFast(), new AntiNCP(), new OldNCPHop(), new OnGround(), new SimpleMotionBoost());
    }

    @Override
    public void onEnable() {
        mc.timer.timerSpeed = 1F;
    }

    @Override
    public void onDisable() {
        mc.thePlayer.speedInAir = 0.02f;
        mc.timer.timerSpeed = 1f;
    }


}
