package tech.imxianyu.module.impl.player;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.NumberSetting;

public class Timer extends Module {

    public NumberSetting<Double> speed = new NumberSetting<Double>("Speed", 1.0, 0.01, 3.0, 0.01) {
        @Override
        public void onValueChanged(Double last, Double now) {
            if (now < 0) {
                this.setValue(last);
            }
        }
    };
    @Handler
    public void onTick(TickEvent event) {
        if (event.isPre() && mc.thePlayer != null && mc.theWorld != null) {
            this.setSuffix(String.valueOf(this.speed.getValue()));
            mc.timer.timerSpeed = this.speed.getFloatValue();
        }
    };

    public Timer() {
        super("Timer", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
    }
}
