package tech.imxianyu.module.impl.movement;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.movement.autosprint.KeyBind;
import tech.imxianyu.module.impl.movement.autosprint.Normal;

/**
 * @author ImXianyu
 * @since 4/15/2023 7:39 PM
 */
public class AutoSprint extends Module {

    public AutoSprint() {
        super("Auto Sprint", Category.MOVEMENT);
        super.addSubModules(new KeyBind()/*, new Normal()*/);
        super.setDescription("Allows you to sprint automatically.");
    }
}
