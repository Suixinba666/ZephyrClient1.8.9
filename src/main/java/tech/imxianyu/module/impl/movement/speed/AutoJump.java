package tech.imxianyu.module.impl.movement.speed;


import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.impl.movement.Speed;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.entity.PlayerUtils;

public class AutoJump extends SubModule<Speed> {

    public AutoJump() {
        super("AutoJump");
    }
    @Handler
    public void onTick(TickEvent event) {
        if (!event.isPre())
            return;

        if (mc.thePlayer.isInWater())
            return;

        if (PlayerUtils.isMoving2()) {
            if (mc.thePlayer.onGround)
                mc.thePlayer.jump();
        }
    };

}
