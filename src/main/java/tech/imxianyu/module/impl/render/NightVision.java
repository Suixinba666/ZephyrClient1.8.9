package tech.imxianyu.module.impl.render;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;

public class NightVision extends Module {

    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;
        if (!mc.thePlayer.isPotionActive(Potion.nightVision.id)) {
            mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 1000000, 255, false, false));
        }
    };

    public NightVision() {
        super("Night Vision", Category.RENDER);
    }
}
