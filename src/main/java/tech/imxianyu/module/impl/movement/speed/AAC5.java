package tech.imxianyu.module.impl.movement.speed;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.impl.movement.Speed;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.entity.PlayerUtils;

import java.util.Random;

public class AAC5 extends SubModule<Speed> {

    public AAC5() {
        super("AAC5");
    }
    @Handler
    public void onTick(TickEvent event) {
        if (!event.isPre())
            return;

        if (!PlayerUtils.isMoving2()) {
            return;
        }
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
            mc.thePlayer.speedInAir = 0.02F;
            mc.timer.timerSpeed = 0.94F;
        }
        if (mc.thePlayer.fallDistance < 0.5) {
//                mc.thePlayer.speedInAir = 0.02003F;
            mc.timer.timerSpeed = 0.94F - new Random().nextInt(100) / 5000f;
        }
        if (mc.thePlayer.fallDistance > 0.5 && mc.thePlayer.fallDistance < 1.3) {
//                mc.thePlayer.speedInAir = 0.020013F;
            mc.timer.timerSpeed = 2.6F;
//                mc.thePlayer.motionY -= 0.;
        } else if (mc.thePlayer.fallDistance > 1.3) {
//                mc.timer.timerSpeed = 1.4F;

            mc.timer.timerSpeed = 1.15F;
        }
    };


}
