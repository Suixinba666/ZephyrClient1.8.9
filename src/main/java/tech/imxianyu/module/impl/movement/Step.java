package tech.imxianyu.module.impl.movement;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.movement.step.NCP;
import tech.imxianyu.module.impl.movement.step.Vanilla;
import tech.imxianyu.settings.NumberSetting;

/**
 * @author ImXianyu
 * @since 7/6/2023 7:29 PM
 */
public class Step extends Module {

    public Step() {
        super("Step", Category.MOVEMENT);
        super.addSubModules(new Vanilla(), new NCP());
    }

    public NumberSetting<Double> height = new NumberSetting<>("Height", 2.0D, 0.0D, 10.0D, 0.1D);
    public NumberSetting<Integer> delay = new NumberSetting<>("Delay", 0, 0, 1000, 1);

    @Override
    public void onDisable() {
        if (this.mc.thePlayer != null) {
            this.mc.thePlayer.stepHeight = 0.5f;
        }
        mc.timer.timerSpeed = 1.0F;
    }

    @Handler
    public void onUpdate(UpdateEvent event) {
        this.setSuffix(this.getCurrentSubModule().getName());
        if (mc.timer.timerSpeed < 1 && mc.thePlayer.onGround) {
            mc.timer.timerSpeed = 1;
        }
    };


}
