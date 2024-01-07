package tech.imxianyu.module.impl.movement;

import lombok.val;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import tech.imxianyu.eventapi.EventCancellable;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.SlowDownEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.utils.timing.Timer;

public class NoSlow extends Module {

    @Handler
    public void onSlowDown(SlowDownEvent event) {
        event.setCancelled();
    }
    public ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.NCP);
    public Timer msTimer = new Timer();
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        this.setSuffix(this.mode.getCurMode());

        if (!PlayerUtils.isMoving2()) {
            return;
        }

        if (!mc.thePlayer.isBlocking())
            return;

        switch (this.mode.getValue()) {
            case NCP: {
                sendPacket(event, true, true, false, 0, false, false);
                break;
            }

            case WatchDog: {
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    sendPacket(event, true, false, true, 50, true, false);
                } else {
                    sendPacket(event, false, true, false, 0, true, true);
                }
                break;
            }

            case WatchDog2: {
                if (event.isPre()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                } else {
                    mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f));
                }
                break;
            }
        }
    };

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT);
    }

    private void sendPacket(UpdateEvent event, boolean sendC07, boolean sendC08, boolean delay, long delayValue, boolean onGround, boolean watchDog) {
        val digging = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(-1, -1, -1), EnumFacing.DOWN);
        val blockPlace = new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem());
        val blockMent = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f);
        if (onGround && !mc.thePlayer.onGround) {
            return;
        }
        if (sendC07 && event.isPre()) {
            if (delay && msTimer.isDelayed(delayValue)) {
                mc.thePlayer.sendQueue.addToSendQueue(digging);
            } else if (!delay) {
                mc.thePlayer.sendQueue.addToSendQueue(digging);
            }
        }
        if (sendC08 && !event.isPre()) {
            if (delay && msTimer.isDelayed(delayValue) && !watchDog) {
                mc.thePlayer.sendQueue.addToSendQueue(blockPlace);
                msTimer.reset();
            } else if (!delay && !watchDog) {
                mc.thePlayer.sendQueue.addToSendQueue(blockPlace);
            } else if (watchDog) {
                mc.thePlayer.sendQueue.addToSendQueue(blockMent);
            }
        }
    }

    public enum Mode {
        NCP,
        WatchDog,
        WatchDog2,
        Vanilla
    }
}
