package tech.imxianyu.module.impl.movement;

import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.ReceivePacketEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.movement.fly.AirJump;
import tech.imxianyu.module.impl.movement.fly.Glide;
import tech.imxianyu.module.impl.movement.fly.Vanilla;
import tech.imxianyu.module.impl.movement.fly.ZoomFly;
import tech.imxianyu.rendering.notification.Notification;
import tech.imxianyu.rendering.notification.NotificationManager;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.timing.Timer;

public class Fly extends Module {

    public NumberSetting<Double> timerBoost = new NumberSetting<>("Timer Boost", 1.0, 0.1, 6.0, 0.1);

    public BooleanSetting withBoostTime = new BooleanSetting("With Boost Time", false);

    public NumberSetting<Double> boostTime = new NumberSetting<>("Boost Time (Seconds)", 3.0, 0.0, 10.0, 0.01, () -> withBoostTime.getValue());
    public BooleanSetting instant = new BooleanSetting("Instant Boost", false);


    public BooleanSetting blinkFly = new BooleanSetting("Blink Fly", true);
    public NumberSetting<Long> blinkTime = new NumberSetting<>("Blink Time", 100L, 1L, 1000L, 1L, () -> blinkFly.getValue());
    public BooleanSetting lagBackCheck = new BooleanSetting("Lag Back Check", true);
    @Handler
    public void onReceive(ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && this.lagBackCheck.getValue()) {
            this.toggle();
            NotificationManager.show("Disabled " + this.getName() + " due to lag back.", Notification.Type.WARNING);
        }
    };
    Timer blinkTimer = new Timer();
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        if (blinkFly.getValue() && blinkTimer.isDelayed(blinkTime.getValue(), true)) {
            ModuleManager.blink.toggle();
            ModuleManager.blink.toggle();
        }
    };
    boolean timerBoosting = false;
    Timer timerTimer = new Timer();
    @Handler
    public void onTick(TickEvent event) {
        this.setSuffix(this.getSubModes().getValue());

        if (timerBoosting) {
            mc.timer.timerSpeed = this.timerBoost.getFloatValue();
            if (withBoostTime.getValue() && timerTimer.isDelayed((long) (this.boostTime.getValue() * 1000L), true)) {
                timerBoosting = false;
                mc.timer.timerSpeed = 1.0f;
                if (instant.getValue()) {
                    this.toggle();
                }
            }
        } else {
            mc.timer.timerSpeed = 1.0f;
        }
    };

    public Fly() {
        super("Fly", Category.MOVEMENT);
        super.addSubModules(new ZoomFly(), new Vanilla(), new AirJump(), new Glide());
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null && mc.theWorld != null) {
            this.timerBoosting = true;
            this.timerTimer.reset();
            if (blinkFly.getValue() && !ModuleManager.blink.isEnabled()) {
                ModuleManager.blink.toggle();
            }
        }

    }

    @Override
    public void onDisable() {
        if (blinkFly.getValue() && ModuleManager.blink.isEnabled()) {
            ModuleManager.blink.toggle();
        }
    }
}
