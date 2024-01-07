package tech.imxianyu.module.impl.movement;

import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.movement.velocity.Simple;

/**
 * @author ImXianyu
 * @since 6/24/2023 11:39 AM
 */
public class Velocity extends Module {

    public Velocity() {
        super("Velocity", Category.MOVEMENT);
        super.addSubModules(new Simple());
    }
}
