package tech.imxianyu.module.impl.combat.criticals;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.impl.combat.Criticals;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.ModeSetting;

public class Motion extends SubModule<Criticals> {

    public Motion() {
        super("Motion");
    }

    public ModeSetting<Mode> mode = new ModeSetting<>("Motion Mode", Mode.FullJump);

    @Handler
    public void onTick(TickEvent event) {
        this.getModule().setSuffix("Motion: " + mode.getValue());
    };
    @Handler
    public void onAttack(SendPacketEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity && ((C02PacketUseEntity) event.getPacket()).getAction() == C02PacketUseEntity.Action.ATTACK) {

            Entity ent = ((C02PacketUseEntity) event.getPacket()).getEntityFromWorld(mc.theWorld);

            if (ent instanceof EntityLivingBase && this.getModule().canCrit((EntityLivingBase) ent)) {
                if (this.mode.getValue() == Mode.FullJump) {
                    mc.thePlayer.jump();
                }

                if (this.mode.getValue() == Mode.MiniJump) {
                    mc.thePlayer.motionY = 0.2;
                }

                if (this.mode.getValue() == Mode.LowJump) {
                    mc.thePlayer.motionY = 0.1;
                    mc.thePlayer.fallDistance = 0.1f;
                    mc.thePlayer.onGround = false;
                }

                if (this.mode.getValue() == Mode.TPHop) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.02, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ, false));
//                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ);
                }

                this.getModule().timer.reset();
            }
        }
    };

    public enum Mode {
        FullJump,
        MiniJump,
        LowJump,
        TPHop
    }

}
