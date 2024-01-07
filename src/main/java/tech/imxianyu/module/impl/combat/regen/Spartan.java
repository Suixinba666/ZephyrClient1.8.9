package tech.imxianyu.module.impl.combat.regen;

import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.impl.combat.Regen;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.utils.timing.Timer;

public class Spartan extends SubModule<Regen> {

    public Spartan() {
        super("Spartan");
    }

    private NumberSetting<Integer> delay = new NumberSetting<>("Delay", 200,0, 10000, 1);
    private NumberSetting<Integer> speed = new NumberSetting<>("Speed", 100, 1, 100, 1);
    private NumberSetting<Integer> health = new NumberSetting<>("Health", 18, 0, 20, 1);
    private NumberSetting<Integer> food = new NumberSetting<>("Food", 18, 0, 20, 1);
    private BooleanSetting noAir = new BooleanSetting("NoAir", false);
    private BooleanSetting potionEffect = new BooleanSetting("PotionEffect", false);

    private Timer timer = new Timer();

    private boolean resetTimer = false;

    @Handler
    public void onUpdate(UpdateEvent event) {
        if (resetTimer) {
            mc.timer.timerSpeed = 1F;
        }

        if ((!noAir.getValue() || mc.thePlayer.onGround) && !mc.thePlayer.capabilities.isCreativeMode && mc.thePlayer.getFoodStats().getFoodLevel() > food.getValue() && mc.thePlayer.isEntityAlive() && mc.thePlayer.getHealth() < health.getValue()) {
            if (potionEffect.getValue() && !mc.thePlayer.isPotionActive(Potion.regeneration)) {
                return;
            }

            if (timer.isDelayed(delay.getValue().longValue())) {
                if (!PlayerUtils.isMoving2() && mc.thePlayer.onGround) {
                    for (int i = 0; i < 9; i++) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(mc.thePlayer.onGround));
                    }

                    mc.timer.timerSpeed = 0.45F;
                    resetTimer = true;
                }

                timer.reset();
            }
        }
    }

}
