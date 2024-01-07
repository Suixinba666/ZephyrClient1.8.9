package tech.imxianyu.module.impl.movement;

import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.movement.phase.Clip;
import tech.imxianyu.module.impl.movement.phase.Skip;
import tech.imxianyu.module.impl.movement.phase.Vanilla;
import tech.imxianyu.utils.timing.Timer;

/**
 * @author ImXianyu
 * @since 1/8/2023 11:48 AM
 */
public class Phase extends Module {

    public Phase() {
        super("Phase", Category.MOVEMENT);
        super.addSubModules(new Vanilla(), new Skip(), new Clip());
    }

    public Timer tickTimer = new Timer();

}
