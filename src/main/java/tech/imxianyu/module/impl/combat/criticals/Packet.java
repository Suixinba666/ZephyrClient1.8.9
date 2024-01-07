package tech.imxianyu.module.impl.combat.criticals;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.impl.combat.Criticals;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.ModeSetting;

public class Packet extends SubModule<Criticals> {

    public Packet() {
        super("Packet");
    }

    public ModeSetting<Mode> mode = new ModeSetting<>("Packet Mode", Mode.Adaptive);

    @Handler
    public void onTick(TickEvent event) {
        this.getModule().setSuffix("Packet: " + mode.getValue());
    };

    @Handler
    public void onAttack(SendPacketEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity && ((C02PacketUseEntity) event.getPacket()).getAction() == C02PacketUseEntity.Action.ATTACK) {
            EntityLivingBase target = (EntityLivingBase) ((C02PacketUseEntity) event.getPacket()).getEntityFromWorld(mc.theWorld);
            if (this.getModule().canCrit(target)) {
//                mc.thePlayer.addChatMessage("Crit! " + new Random().nextInt(500));
                double curX = mc.thePlayer.posX;
                double curY = mc.thePlayer.posY;
                double curZ = mc.thePlayer.posZ;
                double[] offsets;
                switch (mode.getValue()) {
                    case Butcher:
                        offsets = new double[]{0.021051615011, 0.01001, 0.01100150315, 0.00100002, 0.00633, 0.0};
                        break;
                    case HypixelHurt:
                        offsets = new double[]{0.03 - 0.003, 0.0, 0.0, 0.06142999976873398, 0.0};
                        break;
                    case Packet:
                        offsets = new double[]{0.06, 0.0};
                        break;
                    case Packet2:
                        offsets = new double[]{0.062, 0.0, 0.03, 0.0};
                        break;
                    case Packet3:
                        offsets = new double[]{0.0625, 0.0, 1.1E-5, 0.0};
                        break;
                    case NCPPacket:
                        offsets = new double[]{0.11, 0.1100013579, 0.0000013579};
                        break;
                    case MinCat:
                        offsets = new double[]{0.05954136143876984, 0.05943483573247983, 0.01354835722479834, 0.0};
                        break;
                    case Edit:
                        offsets = new double[]{0.0, 0.419999986886978, 0.3331999936342235, 0.2481359985909455,
                                0.164773281826067, 0.083077817806467, 0.0, -0.078400001525879, -0.155232004516602,
                                -0.230527368912964, -0.304316827457544, -0.376630498238655, -0.104080378093037};
                        break;
                    case HVH:
                        offsets = new double[]{0.06250999867916107D, -9.999999747378752E-6D, 0.0010999999940395355D};
                        break;
                    default:
                        offsets = new double[]{0.0};
                        break;
                }

                for (double offset : offsets) {
                    if (this.getModule().random.getValue() && offset == offsets[0])
                        offset += randomNumber(-1000, 10000) * 1e-8;
                    this.mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(curX, curY + offset, curZ, false));
                }
                mc.thePlayer.onCriticalHit(target);
                this.getModule().timer.reset();
            }

            if (this.mode.getValue() == Mode.Adaptive) {
                if (this.getModule().canCrit(target)) {
                    double curX = mc.thePlayer.posX;
                    double curY = mc.thePlayer.posY;
                    double curZ = mc.thePlayer.posZ;
                    double[] offsets;
                    if (target.onGround) {
                        if (target.getDistanceToEntity(this.mc.thePlayer) <= 1) {
                            offsets = new double[]{0.05, 0.0};
                        } else {
                            offsets = new double[]{0.03, 0.001 + target.hurtTime * 1e-4, 0.021, 0.0};
                        }
                    } else {
                        offsets = new double[]{0.05, 0.03 + randomNumber(-1000, 10000) * 1e-8, 0.01, 0.0};
                    }
                    if (this.mc.thePlayer.hurtTime > 7) {
                        this.mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.003, mc.thePlayer.posZ, false));
//                        this.mc.thePlayer.motionY = 0.01 * (this.mc.thePlayer.hurtTime - 7);
                    } else {
                        for (double offset : offsets) {
                            if (this.getModule().random.getValue() && offset == offsets[0])
                                offset += randomNumber(-1000, 10000) * 1e-8;
                            this.mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(curX, curY + offset, curZ, false));
                        }
                    }
                    this.getModule().timer.reset();
                }
            }
        }
    };
    @Handler
    public void onSend(SendPacketEvent e) {
        if (e.getPacket() instanceof C03PacketPlayer && this.mode.getValue() == Mode.NoGround) {
            C03PacketPlayer c03PacketPlayer = (C03PacketPlayer) e.getPacket();
            c03PacketPlayer.onGround = false;
            e.setPacket(c03PacketPlayer);
        }
    };

    public static int randomNumber(final int n, final int n2) {
        return Math.round(n2 + (float) Math.random() * (n - n2));
    }

    public enum Mode {
        Adaptive,
        Packet,
        Packet2,
        Packet3,
        NoGround,
        MinCat,
        HypixelHurt,
        Butcher,
        NCPPacket,

        Edit,
        HVH
    }
}
