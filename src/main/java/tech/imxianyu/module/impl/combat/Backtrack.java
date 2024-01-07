package tech.imxianyu.module.impl.combat;

import lombok.AllArgsConstructor;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.ReceivePacketEvent;
import tech.imxianyu.events.player.EntityMovementEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.NumberSetting;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Backtrack extends Module {

    public Backtrack() {
        super("BackTrack", Category.COMBAT);
    }

    public NumberSetting<Integer> maximumDelay = new NumberSetting<>("Max Delay", 250, 1, 1000, 1);
    public NumberSetting<Integer> maximumCachedPositions = new NumberSetting<>("Max Cached Positions", 10, 1, 20, 1);

    Map<UUID, List<BacktrackData>> backtrackedPlayer = new ConcurrentHashMap<>();

    @Handler
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.getPacket() instanceof S0CPacketSpawnPlayer) {
            S0CPacketSpawnPlayer packet = (S0CPacketSpawnPlayer) event.getPacket();

            addBacktrackData(packet.getPlayer(), packet.getX() / 32.0, packet.getY() / 32.0, packet.getZ() / 32.0, packet.getX() / 32.0, packet.getY() / 32.0, packet.getZ() / 32.0, System.currentTimeMillis());
        }

        backtrackedPlayer.forEach((key, backtrackData) -> {

            backtrackData.removeIf(data -> data.time + maximumDelay.getValue() < System.currentTimeMillis());

            if (backtrackData.isEmpty()) {
                removeBacktrackData(key);
            }

        });
    }

    @Override
    public void onEnable() {
        backtrackedPlayer.clear();
    }

    @Handler
    public void onEntityMove(EntityMovementEvent event) {
        Entity entity = event.getMovedEntity();

        if (entity instanceof EntityPlayer) {
            addBacktrackData(entity.getUniqueID(), event.getX(), event.getY(), event.getZ(), entity.prevPosX, entity.prevPosY, entity.prevPosZ, System.currentTimeMillis());
        }

    }

    @Handler
    public void onRender3D(Render3DEvent event) {
        Color color = Color.RED;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer) {
                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_DEPTH_TEST);

                mc.entityRenderer.disableLightmap();

                GL11.glBegin(GL11.GL_LINE_STRIP);
                RenderSystem.color(color.getRGB());

                val renderPosX = mc.getRenderManager().viewerPosX;
                val renderPosY = mc.getRenderManager().viewerPosY;
                val renderPosZ = mc.getRenderManager().viewerPosZ;

                /*loopThroughBacktrackData(entity, () -> {
                    GL11.glVertex3d(entity.posX - renderPosX, entity.posY - renderPosY, entity.posZ - renderPosZ);
                    return false;
                });*/

                List<BacktrackData> backtrackData = getBacktrackData(entity.getUniqueID());

                if (backtrackData != null && !backtrackData.isEmpty()) {
                    for (BacktrackData backtrackDatum : backtrackData) {
                        GL11.glVertex3d(backtrackDatum.x - renderPosX, backtrackDatum.y - renderPosY, backtrackDatum.z - renderPosZ);
                    }


//                    entity.prevPosX = data.prevX;
//                    entity.prevPosY = data.prevY;
//                    entity.prevPosZ = data.prevZ;

                    if (backtrackData.size() > 1) {
                        BacktrackData data = backtrackData.get(1);
                        entity.setPosition(data.x, data.y, data.z);
                        BacktrackData prevData = backtrackData.get(0);

                        entity.prevPosX = prevData.x;
                        entity.prevPosY = prevData.y;
                        entity.prevPosZ = prevData.z;
                    }
                }

//                loopThroughBacktrackData(entity, () -> true);

                GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
                GL11.glEnd();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glPopMatrix();
            }
        }
    }

    public double getNearestTrackedDistance(Entity entity) {
        AtomicReference<Double> nearestRange = new AtomicReference<>(0.0);

        loopThroughBacktrackData(entity, () -> {
            val range = entity.getDistanceToEntity(mc.thePlayer);

            if (range < nearestRange.get() || nearestRange.get() == 0.0) {
                nearestRange.set((double) range);
            }

            return false;
        });

        return nearestRange.get();
    }

    public void loopThroughBacktrackData(Entity entity, Supplier<Boolean> action) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }

        val backtrackDataArray = getBacktrackData(entity.getUniqueID());

        if (backtrackDataArray == null)
            return;

        val entityPosition = new double[] {entity.posX, entity.posY, entity.posZ};
        val prevPosition = new double[] {entity.prevPosX, entity.prevPosY, entity.prevPosZ};

        // This will loop through the backtrack data. We are using reversed() to loop through the data from the newest to the oldest.

        for (BacktrackData backtrackData : backtrackDataArray) {
            entity.setPosition(backtrackData.x, backtrackData.y, backtrackData.z);
            entity.prevPosX = backtrackData.x;
            entity.prevPosY = backtrackData.y;
            entity.prevPosZ = backtrackData.z;
            if (action.get()) {
                break;
            }
        }
        entity.prevPosX = prevPosition[0];
        entity.prevPosY = prevPosition[1];
        entity.prevPosZ = prevPosition[2];

        entity.setPosition(entityPosition[0], entityPosition[1], entityPosition[2]);
    }

    private void addBacktrackData(UUID id, double x, double y, double z, double prevX, double prevY, double prevZ, long time) {
        // Get backtrack data of player
        List<BacktrackData> backtrackData = getBacktrackData(id);

        // Check if there is already data of the player
        if (backtrackData != null) {
            // Check if there is already enough data of the player
            if (backtrackData.size() >= maximumCachedPositions.getValue()) {
                // Remove first data
                backtrackData.remove(0);
            }

            // Insert new data
            backtrackData.add(new BacktrackData(x, y, z, prevX, prevY, prevZ, time));
        } else {
            // Create new list
            backtrackedPlayer.put(id, new ArrayList<>(Collections.singletonList(new BacktrackData(x, y, z, prevX, prevY, prevZ, time))));
        }
    }

    private List<BacktrackData> getBacktrackData(UUID id) {
        return backtrackedPlayer.get(id);
    }

    private void removeBacktrackData(UUID id) {
        backtrackedPlayer.remove(id);
    }


    @AllArgsConstructor
    private class BacktrackData {
        public double x, y, z;
        public double prevX, prevY, prevZ;
        public long time;
    }

}
