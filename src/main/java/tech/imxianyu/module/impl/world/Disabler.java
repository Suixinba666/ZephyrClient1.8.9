package tech.imxianyu.module.impl.world;

import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C18PacketSpectate;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.utils.math.MathUtils;
import tech.imxianyu.utils.timing.Timer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Disabler extends Module {

    public static Timer c13timer = new Timer();
    private final Timer tetimer = new Timer();
    private final List<C0FPacketConfirmTransaction> c0fs = new ArrayList<>();
    public ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Matrix);
    @Handler
    public void onTick(TickEvent event) {
        this.setSuffix(this.mode.getCurMode());
    };
    public ModeSetting<NCPMode> ncpMode = new ModeSetting<>("NCP Mode", NCPMode.C0F, () -> mode.getValue() == Mode.NCP);
    @Handler
    public void onSend(SendPacketEvent event) {
        if (this.mode.getValue() == Mode.Matrix) {
            if (event.getPacket() instanceof C03PacketPlayer) {
                if (mc.thePlayer.ticksExisted % 15 == 0) { // Technically only have to send once, but to be sure it exempts you just send once every 15 ticks.
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(b);
                    try {
                        out.writeUTF(mc.thePlayer.getGameProfile().getName()); // Username of player to exempt
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                    buf.writeBytes(b.toByteArray());
                    mc.thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload("matrix:geyser", buf));
                }
            }
        }

        if (this.mode.getValue() == Mode.NCP) {
            final Packet packet = event.getPacket();
            if (mc.isSingleplayer()) {
                return;
            }
            if (packet instanceof C0FPacketConfirmTransaction) {
                final C0FPacketConfirmTransaction c0f = (C0FPacketConfirmTransaction) packet;
                if (c0f.getWindowId() == 0 && c0f.getUid() < 0) {
                    event.setCancelled();
                    this.c0fs.add(c0f);
                }
                if (this.ncpMode.getValue() == NCPMode.C0F) {
                    try {
                        if (this.c0fs.size() > 7) {
                            for (final C0FPacketConfirmTransaction p2 : this.c0fs) {
                                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(p2);
                            }
                            this.c0fs.clear();
                            Disabler.c13timer.reset();
                        }
                    } catch (final Throwable ignored) {
                    }
                }
            }
        }
    };
    public BooleanSetting C18 = new BooleanSetting("C18", false, () -> this.mode.getValue() == Mode.NCP);
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre() || this.mode.getValue() != Mode.NCP) {
            return;
        }

        if (mc.thePlayer.ridingEntity != null) {
            return;
        }
        if (mc.isSingleplayer()) {
            return;
        }
        if (this.ncpMode.getValue() == NCPMode.C0F && this.tetimer.isDelayed((long) (899 + MathUtils.randomNumber(0, 5)))) {
            if (c13timer.isDelayed(10000)) {
                final short uid = (short) MathUtils.randomNumber(-32768, 0);
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C0FPacketConfirmTransaction(0, uid, false));
            }
            if (this.C18.getValue()) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C18PacketSpectate(mc.thePlayer.getUniqueID()));
            }
            this.tetimer.reset();
        }
    };

    public Disabler() {
        super("Disabler", Category.WORLD);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mode.getValue() == Mode.NCP) {
            c0fs.clear();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mode.getValue() == Mode.NCP) {
            c0fs.clear();
        }
    }

    public enum Mode {
        Matrix,
        NCP
    }

    public enum NCPMode {
        PingSpoof,
        C0F
    }
}