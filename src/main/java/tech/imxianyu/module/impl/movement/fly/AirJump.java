package tech.imxianyu.module.impl.movement.fly;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.impl.movement.Fly;
import tech.imxianyu.module.submodule.SubModule;

/**
 * @author ImXianyu
 * @since 1/8/2023 8:51 PM
 */
public class AirJump extends SubModule<Fly> {
    public AirJump() {
        super("AirJump");
    }

    double startY;
    @Handler
    public void onTick(TickEvent event) {
        if (!event.isPre())
            return;

        if (mc.thePlayer.posY <= startY)
            mc.thePlayer.motionY = mc.thePlayer.getJumpUpwardsMotion();
    };

    @Override
    public void onEnable() {
        if (mc.thePlayer != null)
            startY = mc.thePlayer.posY;
    }
}
