package tech.imxianyu.module.impl.movement;

import net.minecraft.network.play.client.C03PacketPlayer;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.timing.Timer;

import java.lang.annotation.Target;

public class FlagFly extends Module {

    public FlagFly(){
        super("FlagFly", Category.MOVEMENT);
    }

    public NumberSetting<Integer> delay = new NumberSetting<>("Delay", 50, 0, 1000, 1);
    public NumberSetting<Integer> downValue = new NumberSetting<>("Down Value", -6969, -10000, -10, 1);

    public BooleanSetting moveForward = new BooleanSetting("Move Forward", false);
    public BooleanSetting deltaMove = new BooleanSetting("Delta Move", false);

    Timer timer = new Timer();

    private void sendPacket(){
//        mc.thePlayer.motionX = 1;
        if (timer.isDelayed(delay.getValue())) {
            if (moveForward.getValue()) {
                mc.thePlayer.motionX = 0.42;
            }

            double value = downValue.getValue();

            if (deltaMove.getValue()) {
                value = mc.thePlayer.posY + value;
            }

            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, value, mc.thePlayer.posZ, true));
            mc.thePlayer.setPosition(mc.thePlayer.posX, value, mc.thePlayer.posZ);
            timer.reset();
        }
    }

    @Handler
    public void onUpdate(UpdateEvent event){
        this.sendPacket();
    }
}
