package tech.imxianyu.module.impl.movement.autosprint;

import net.minecraft.client.settings.KeyBinding;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.movement.AutoSprint;
import tech.imxianyu.module.submodule.SubModule;

public class KeyBind extends SubModule<AutoSprint> {

    public KeyBind() {
        super("KeyBind");
    }

    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        this.getModule().setSuffix("KeyBind");

        if (event.isPre()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), !ModuleManager.blockFly.isEnabled() || ModuleManager.blockFly.sprint.getValue());
        }
    };

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
    }
}
