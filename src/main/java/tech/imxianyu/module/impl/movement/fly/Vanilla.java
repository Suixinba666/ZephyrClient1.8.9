package tech.imxianyu.module.impl.movement.fly;

import net.minecraft.world.WorldSettings;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.impl.movement.Fly;
import tech.imxianyu.module.submodule.SubModule;

public class Vanilla extends SubModule<Fly> {

    public Vanilla() {
        super("Vanilla");
    }
    @Handler
    public void onTick(TickEvent event) {
        if (!event.isPre())
            return;


        mc.thePlayer.capabilities.allowFlying = true;
        mc.thePlayer.capabilities.isFlying = true;
    };

    @Override
    public void onEnable() {
        mc.thePlayer.capabilities.allowFlying = true;
        mc.thePlayer.capabilities.isFlying = true;
    }

    @Override
    public void onDisable() {

        if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SURVIVAL)
            mc.thePlayer.capabilities.allowFlying = false;

        mc.thePlayer.capabilities.isFlying = false;
    }
}
