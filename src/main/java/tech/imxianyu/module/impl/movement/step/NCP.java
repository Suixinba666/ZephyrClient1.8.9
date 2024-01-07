package tech.imxianyu.module.impl.movement.step;

import net.minecraft.network.play.client.C03PacketPlayer;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.StepEvent;
import tech.imxianyu.module.impl.movement.Step;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.timing.Timer;

import java.util.Arrays;
import java.util.List;

/**
 * @author ImXianyu
 * @since 7/6/2023 7:33 PM
 */
public class NCP extends SubModule<Step> {

    public NCP() {
        super("NCP");
    }
    
    boolean resetTimer;

    Timer timer = new Timer();

    @Override
    public void onEnable() {
        this.resetTimer = false;
    }

    @Handler
    public void onStep(StepEvent event) {
        if (mc.thePlayer.isInWater())
            return;

        if(event.isPre()) {
            if (this.resetTimer) {
                this.resetTimer = false;
                mc.timer.timerSpeed = 1.0f;
            }
            if (!this.mc.thePlayer.onGround || !this.timer.isDelayed(this.getModule().delay.getValue().longValue())) {
                event.setStepHeight(this.mc.thePlayer.stepHeight = 0.5f);
                return;
            }
            this.mc.thePlayer.stepHeight = this.getModule().height.getValue().floatValue();
            event.setStepHeight(this.getModule().height.getValue().floatValue());
        } else {
            if (event.getStepHeight() > 0.5) {
                final double n = this.mc.thePlayer.getEntityBoundingBox().minY - this.mc.thePlayer.posY;
                if (n >= 0.625) {
                    final float n2 = 0.6f;
                    float n3;
                    if (n >= 1.0) {
                        n3 = Math.abs(1.0f - (float) n) * 0.33f;
                    } else {
                        n3 = 0.0f;
                    }
                    mc.timer.timerSpeed = n2 - n3;
                    if (mc.timer.timerSpeed <= 0.05f) {
                        mc.timer.timerSpeed = 0.05f;
                    }
                    this.resetTimer = true;
                    this.ncpStep(n);
                    this.timer.reset();
                }
            }
        }
    };

    void ncpStep(final double n) {
        final List<Double> list = Arrays.asList(0.42, 0.333, 0.248, 0.083, -0.078);
        final double posX = this.mc.thePlayer.posX;
        final double posZ = this.mc.thePlayer.posZ;
        double posY = this.mc.thePlayer.posY;
        if (n < 1.1) {
            double n2 = 0.42;
            double n3 = 0.75;
            if (n != 1.0) {
                n2 *= n;
                n3 *= n;
                if (n2 > 0.425) {
                    n2 = 0.425;
                }
                if (n3 > 0.78) {
                    n3 = 0.78;
                }
                if (n3 < 0.49) {
                    n3 = 0.49;
                }
            }
            if (n2 == 0.42) {
                n2 = 0.41999998688698;
            }
            this.mc.thePlayer.sendQueue
                    .addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY + n2, posZ, false));
            if (posY + n3 < posY + n) {
                this.mc.thePlayer.sendQueue
                        .addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY + n3, posZ, false));
            }
            return;
        }
        if (n < 1.6) {
            int i = 0;
            while (i < list.size()) {
                posY += list.get(i);
                this.mc.thePlayer.sendQueue
                        .addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY, posZ, false));
                ++i;
            }
        } else if (n < 2.1) {
            final double[] array = { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869 };
            final int length = array.length;
            int j = 0;
            while (j < length) {
                this.mc.thePlayer.sendQueue.addToSendQueue(
                        new C03PacketPlayer.C04PacketPlayerPosition(posX, posY + array[j], posZ, false));
                ++j;
            }
        } else {
            final double[] array2 = { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907 };
            final int length2 = array2.length;
            int k = 0;
            while (k < length2) {
                this.mc.thePlayer.sendQueue.addToSendQueue(
                        new C03PacketPlayer.C04PacketPlayerPosition(posX, posY + array2[k], posZ, false));
                ++k;
            }
        }
    }

}
