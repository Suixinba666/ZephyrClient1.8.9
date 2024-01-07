package tech.imxianyu.module.impl.player;

import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.utils.player.BlockUtils;
import tech.imxianyu.utils.timing.Timer;

public class FastUse extends Module {

    public static ModeSetting<Mode> mode = new ModeSetting("Mode", Mode.NCP);
    public static BooleanSetting inAir = new BooleanSetting("In Air", false);
    @Handler
    public void onTick(TickEvent event) {
        this.setSuffix(mode.getCurMode());
    };
    boolean usedTimer = false;
    Timer msTimer = new Timer();
    private void stopUsing() {
        mc.playerController.onStoppedUsingItem(mc.thePlayer);
    }
    @Handler
    public void onTick2(TickEvent event) {
        if (!event.isPre())
            return;

        if (usedTimer) {
            mc.timer.timerSpeed = 1F;
            usedTimer = false;
        }

        ItemStack it = mc.thePlayer.getItemInUse();
        if (it == null)
            return;

        Item item = it.getItem();

        if (!(item instanceof ItemFood || item instanceof ItemBucketMilk || item instanceof ItemPotion)) {
            return;
        }
        if (mode.getValue() == Mode.NCP) {
            if ((inAir.getValue() || this.mc.thePlayer.onGround) && notBad() && this.mc.thePlayer.getItemInUseDuration() > 14) {
                send(20);
                stopUsing();
            }
        } else if (mode.getValue() == Mode.Instant) {
            if (inAir.getValue() || this.mc.thePlayer.onGround) {
                send(35);
                stopUsing();
            }
        } else if (mode.getValue() == Mode.Matrix) {
            mc.timer.timerSpeed = 0.5f;
            usedTimer = true;
            send(1);
        } else if (mode.getValue() == Mode.Fast) {
            if (mc.thePlayer.getItemInUseDuration() < 25) {
                mc.timer.timerSpeed = 0.3f;
                usedTimer = true;
                send(5);
            }
        } else if (mode.getValue() == Mode.Medusa) {
            if (mc.thePlayer.getItemInUseDuration() > 5 || !msTimer.isDelayed(360L))
                return;

            send(20);

            msTimer.reset();
        } else if (mode.getValue() == Mode.AAC) {
            mc.timer.timerSpeed = 0.49F;
            usedTimer = true;
            if (mc.thePlayer.getItemInUseDuration() > 14) {
                send(23);
            }
        } else if (mode.getValue() == Mode.AACNew) {
            mc.timer.timerSpeed = 0.49F;
            usedTimer = true;
            send(2);
        }
    };

    void send(int times) {
        for (int i = 0; i < times; ++i) {
            this.mc.getNetHandler().addToSendQueue(new C03PacketPlayer(this.mc.thePlayer.onGround));
        }
    }

    public FastUse() {
        super("FastUse", Category.PLAYER);
    }

    private boolean notBad() {
        return !(!this.mc.thePlayer.isInWater() && BlockUtils.isInLiquid());
    }

    public enum Mode {
        NCP,
        Instant,
        Matrix,
        Fast,
        Medusa,
        AAC,
        AACNew,


    }
}
