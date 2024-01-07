package tech.imxianyu.module.impl.movement;

import net.minecraft.client.settings.KeyBinding;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.MoveEntityRotationEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.rotation.RotationUtils;

/**
 * @author ImXianyu
 * @since 1/8/2023 9:19 PM
 */
public class AutoWalkToPit extends Module {

    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !event.isPre())
            return;

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), mc.thePlayer.getDistance(0.5, 82, 0.5) > 0.25);
    };
    @Handler
    public void onMoveRotation(MoveEntityRotationEvent event) {
        float[] rotations = RotationUtils.getRotations(0.5, 82, 0.5);
        event.rotationYaw = rotations[0];
    };
    public NumberSetting<Double> yLevel = new NumberSetting<>("Y Level", 90.0, 0.0, 256.0, 0.5);

    public AutoWalkToPit() {
        super("Auto Walk To Pit", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
    }
}
