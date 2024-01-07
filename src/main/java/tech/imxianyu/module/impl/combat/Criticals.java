package tech.imxianyu.module.impl.combat;

import net.minecraft.entity.EntityLivingBase;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.combat.criticals.DCJ;
import tech.imxianyu.module.impl.combat.criticals.Motion;
import tech.imxianyu.module.impl.combat.criticals.Packet;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.timing.Timer;

public class Criticals extends Module {


    public Timer timer = new Timer();

    public NumberSetting<Integer> hurttime = new NumberSetting<>("Hurt Time", 15, 0, 20, 1);
    public NumberSetting<Integer> delay = new NumberSetting<>("Delay", 200, 0, 1000, 1);
    public BooleanSetting random = new BooleanSetting("Random", true);

    public Criticals() {
        super("Criticals", Category.COMBAT);
        super.addSubModules(new Packet(), new Motion(), new DCJ());
    }

    public boolean canCrit(final EntityLivingBase target) {

        if (ModuleManager.autoEat.isEnabled() && ModuleManager.autoEat.eating)
            return false;

        if (ModuleManager.blockFly.isEnabled())
            return false;

        if (ModuleManager.fly.isEnabled()) {
//            mc.thePlayer.addChatMessage("fly");
            return false;
        }

        if (target.hurtTime > hurttime.getValue()) {
//            mc.thePlayer.addChatMessage("hurt time");
            return false;
        }

        if (mc.thePlayer.isInWater()) {
//            mc.thePlayer.addChatMessage("water");
            return false;
        }

        if (!mc.thePlayer.onGround) {
//            mc.thePlayer.addChatMessage("onGround");
            return false;
        }

        if (!mc.thePlayer.isCollidedVertically) {
//            mc.thePlayer.addChatMessage("collidedVertically");
            return false;
        }

        if (!timer.isDelayed(delay.getValue())) {
//            mc.thePlayer.addChatMessage("Timer");
            return false;
        }

        //            mc.thePlayer.addChatMessage("ridingEntity");
        return mc.thePlayer.ridingEntity == null;
    }
}
