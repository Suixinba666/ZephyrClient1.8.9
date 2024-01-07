package tech.imxianyu.module.impl.movement.autosprint;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.movement.AutoSprint;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.utils.entity.PlayerUtils;

public class Normal extends SubModule<AutoSprint> {

    public Normal() {
        super("Normal");
    }
    public BooleanSetting omni = new BooleanSetting("Omni Direction", false);

    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        this.getModule().setSuffix("Normal" + (omni.getValue() ? ", Omni" : ""));

        if (!this.mc.thePlayer.isCollidedHorizontally && !this.mc.thePlayer.isSneaking() && this.mc.thePlayer.getFoodStats().getFoodLevel() > 6 && (this.omni.getValue() ? PlayerUtils.isMoving2() : this.mc.thePlayer.moveForward > 0.0f) && (!ModuleManager.blockFly.isEnabled() || ModuleManager.blockFly.sprint.getValue())) {
            this.mc.thePlayer.setSprinting(true);
        }
    };
}
