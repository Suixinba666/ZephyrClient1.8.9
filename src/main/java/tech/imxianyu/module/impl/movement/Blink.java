package tech.imxianyu.module.impl.movement;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import org.lwjglx.input.Keyboard;
import tech.imxianyu.eventapi.EventBus;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.events.world.WorldChangedEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.notification.Notification;
import tech.imxianyu.rendering.notification.NotificationManager;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.timing.Timer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Blink
        extends Module {

    private final List<Packet<?>> packets = new ArrayList<>();
    private final LinkedList<double[]> positions = new LinkedList<>();
    @Handler
    public void onWorldChanged(WorldChangedEvent event) {
        EventBus.unregister(this);
        packets.clear();
        positions.clear();
        this.setEnabled(false);
    };
    @Handler
    public void onRender3D(Render3DEvent event) {
        synchronized (positions) {
            glPushMatrix();

            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            mc.entityRenderer.disableLightmap();
            glBegin(GL_LINE_STRIP);
            //RenderUtil.glColor(color);
            RenderSystem.color(Color.YELLOW.getRGB());
            final double renderPosX = mc.getRenderManager().viewerPosX;
            final double renderPosY = mc.getRenderManager().viewerPosY;
            final double renderPosZ = mc.getRenderManager().viewerPosZ;

            for (final double[] pos : positions)
                glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ);

            glColor4d(1, 1, 1, 1);
            glEnd();
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glPopMatrix();
        }
    };
    private final BooleanSetting pulse = new BooleanSetting("Pulse", false);
    private final NumberSetting<Integer> pulseDelay = new NumberSetting<>("Pulse Delay", 1000, 1, 100000, 1, pulse::getValue);

    private final Timer pulseTimer = new Timer();
    double posX, posY, posZ, motionX, motionY, motionZ;
    float rotYaw, rotPitch;
    boolean hold = false;
    private EntityOtherPlayerMP fakePlayer = null;
    private boolean disableLogger;
    @Handler
    public void onSend(SendPacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (mc.thePlayer == null || disableLogger)
            return;

        /*if (packet instanceof C03PacketPlayer) // Cancel all movement stuff
            event.setCancelled(true);

        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition || packet instanceof C03PacketPlayer.C06PacketPlayerPosLook ||
                packet instanceof C08PacketPlayerBlockPlacement ||
                packet instanceof C0APacketAnimation ||
                packet instanceof C0BPacketEntityAction || packet instanceof C02PacketUseEntity) {

        }*/
        event.setCancelled();

        packets.add(packet);
    };
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (event.isPre())
            return;

        synchronized (positions) {
            positions.add(new double[]{mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ});
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD0) && !hold) {
            hold = true;
            mc.thePlayer.addChatMessage("[Blink] Ok cleared");

            /*mc.thePlayer.posX = posX;
            mc.thePlayer.posY = posY;
            mc.thePlayer.posZ = posZ;
            mc.thePlayer.motionX = motionX;
            mc.thePlayer.motionY = motionY;
            mc.thePlayer.motionZ = motionZ;
            mc.thePlayer.rotationYaw = rotYaw;
            mc.thePlayer.rotationPitch = rotPitch;*/
            mc.thePlayer.setPositionAndRotation(posX, posY, posZ, rotYaw, rotPitch);

            packets.clear();

            synchronized (positions) {
                positions.clear();
                positions.add(new double[]{mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (mc.thePlayer.getEyeHeight() / 2), mc.thePlayer.posZ});
                positions.add(new double[]{mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ});
            }

            pulseTimer.reset();
        }

        if (!Keyboard.isKeyDown(Keyboard.KEY_NUMPAD0))
            hold = false;

        if (pulse.getValue() && pulseTimer.isDelayed(pulseDelay.getValue())) {
            blink();
            pulseTimer.reset();
        }
    };

    public Blink() {
        super("Blink", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null)
            return;

        if (mc.theIntegratedServer != null) {
            NotificationManager.show("Do NOT EVER enable blink in singleplayer!", Notification.Type.ERROR, 5000);
            EventBus.unregister(this);
            packets.clear();
            positions.clear();
            this.setEnabled(false);
            return;
        }

        posX = mc.thePlayer.posX;
        posY = mc.thePlayer.posY;
        posZ = mc.thePlayer.posZ;
        motionX = mc.thePlayer.motionX;
        motionY = mc.thePlayer.motionY;
        motionZ = mc.thePlayer.motionZ;
        rotYaw = mc.thePlayer.rotationYaw;
        rotPitch = mc.thePlayer.rotationPitch;

        /*fakePlayer = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
        fakePlayer.clonePlayer(mc.thePlayer, true);
        fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer);
        fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead;
        mc.theWorld.addEntityToWorld(-9100, fakePlayer);*/

        synchronized (positions) {
            positions.add(new double[]{mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (mc.thePlayer.getEyeHeight() / 2), mc.thePlayer.posZ});
            positions.add(new double[]{mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ});
        }

        pulseTimer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null/* || fakePlayer == null*/)
            return;

        blink();
//        mc.theWorld.removeEntityFromWorld(fakePlayer.getEntityId());
        fakePlayer = null;
    }

    public String getTag() {
        return String.valueOf(packets.size());
    }

    private void blink() {
        try {
            disableLogger = true;

            Iterator<Packet<?>> packetIterator = packets.iterator();
            while (packetIterator.hasNext()) {
                mc.getNetHandler().addToSendQueue(packetIterator.next());
                packetIterator.remove();
            }

            disableLogger = false;
        } catch (final Exception e) {
            e.printStackTrace();
            disableLogger = false;
        }

        synchronized (positions) {
            positions.clear();
        }
    }
}
