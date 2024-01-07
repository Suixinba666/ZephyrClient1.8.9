package tech.imxianyu.module.impl.movement;

import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;

public class KeepSprint extends Module {
    public BooleanSetting newMethod = new BooleanSetting("New Bypass Method", false);

    public KeepSprint() {
        super("KeepSprint", Category.MOVEMENT);
    }

}
