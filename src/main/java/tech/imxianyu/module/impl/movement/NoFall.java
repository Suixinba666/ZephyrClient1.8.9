package tech.imxianyu.module.impl.movement;

import net.minecraft.network.play.client.C03PacketPlayer;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.NumberSetting;

public class NoFall extends Module {

    public NoFall() {
        super("No Fall", Category.MOVEMENT);
    }

    public NumberSetting<Double> fallDistance = new NumberSetting<>("Fall Distance", 3.0, 1.0, 4.0, 0.1);


    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        if (ModuleManager.fly.isEnabled())
            return;

        if (mc.thePlayer.fallDistance > fallDistance.getValue()) {
            if (isOnGround(0.001) || (mc.thePlayer.motionY >= 0.0)) return;
            event.setOnGround(true);
        }
        else if (mc.thePlayer.isCollidedVertically) {
            mc.thePlayer.fallDistance = 0.0f;
        }
    };

    public boolean isOnGround(final double n) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -n, 0.0)).isEmpty();
    }

}
