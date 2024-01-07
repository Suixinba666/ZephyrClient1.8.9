package tech.imxianyu.module.impl.combat;

import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.combat.regen.Spartan;
import tech.imxianyu.module.impl.combat.regen.Vanilla;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;

public class Regen extends Module {

    public Regen() {
        super("Regen", Category.COMBAT);
        super.addSubModules(new Vanilla(), new Spartan());
    }

}
