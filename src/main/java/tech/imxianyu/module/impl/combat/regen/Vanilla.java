package tech.imxianyu.module.impl.combat.regen;

import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.impl.combat.Regen;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.player.BlockUtils;
import tech.imxianyu.utils.timing.Timer;

public class Vanilla extends SubModule<Regen> {

    public Vanilla() {
        super("Vanilla");
    }

    public BooleanSetting onlyWhenRegeneration = new BooleanSetting("Only When Regeneration", false);

    public NumberSetting<Integer> oldSpeed = new NumberSetting<>("Speed (PPS)", 30, 10, 100, 10, () -> !onlyWhenRegeneration.getValue());


    public BooleanSetting regeneration = new BooleanSetting("With Regeneration", false, () -> !onlyWhenRegeneration.getValue());
    public NumberSetting<Integer> regenerationSpeed = new NumberSetting<>("Regeneration Speed (PPS)", 30, 10, 100, 10, () -> regeneration.getValue());

    public BooleanSetting realGround = new BooleanSetting("Real Ground State", false);

    Timer regTimer = new Timer();
    Timer oldTimer = new Timer();

    @Handler
    private void onUpdate(final UpdateEvent event) {
        if (!event.isPre())
            return;

        if (!onlyWhenRegeneration.getValue() && oldTimer.isDelayed(1000 / oldSpeed.getValue(), true) && mc.thePlayer.getActivePotionEffect(Potion.regeneration) == null && mc.thePlayer.getHealth() < mc.thePlayer.getMaxHealth() && mc.thePlayer.getFoodStats().getFoodLevel() > 0) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(!realGround.getValue() || mc.thePlayer.onGround));
//            mc.thePlayer.addChatMessage("Lul");
        }

        if (regeneration.getValue()) {

            if (regTimer.isDelayed(1000 / regenerationSpeed.getValue(), true) && mc.thePlayer.getActivePotionEffect(Potion.regeneration) != null && (mc.thePlayer.onGround || BlockUtils.isOnLadder() || BlockUtils.isInLiquid() || BlockUtils.isOnLiquid()) && mc.thePlayer.getHealth() < mc.thePlayer.getMaxHealth()) {
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer(!realGround.getValue() || mc.thePlayer.onGround));
            }
        }
    }

}
