package tech.imxianyu.module.impl.combat;

import tech.imxianyu.module.Module;
import tech.imxianyu.settings.NumberSetting;

/**
 * @author ImXianyu
 * @since 2022/7/17 8:55
 */
public class Reach extends Module {

    public NumberSetting<Double> range = new NumberSetting<>("Range", 3.0, 3.0, 6.0, 0.01);

    public Reach(){
        super("Reach", Category.COMBAT);
    }

}
