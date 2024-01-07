package tech.imxianyu.module.impl.movement.step;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.StepEvent;
import tech.imxianyu.module.impl.movement.Step;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.entity.PlayerUtils;

/**
 * @author ImXianyu
 * @since 7/6/2023 7:30 PM
 */
public class Vanilla extends SubModule<Step> {

    public Vanilla() {
        super("Vanilla");
    }

    @Handler
    public void onStep(StepEvent event) {
        if (mc.thePlayer.isInWater())
            return;

        event.setStepHeight(this.getModule().height.getValue());
    };

}
