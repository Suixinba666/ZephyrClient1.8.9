package tech.imxianyu.module.impl.world.antibots;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.impl.world.AntiBots;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.BooleanSetting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Hypixel extends SubModule<AntiBots> {

    public Hypixel() {
        super("Hypixel");
    }

    public final Set<EntityPlayer> bots = new HashSet<>();
    public BooleanSetting remove = new BooleanSetting("RemoveBot", true);
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (event.isPre()) {
            List<EntityPlayer> playerEntities = new ArrayList<>(mc.theWorld.playerEntities);
            int playerEntitiesSize = playerEntities.size();

            for (EntityPlayer playerEntity : playerEntities) {
                if ((playerEntity.getName().startsWith("§") || this.isEntityBot(playerEntity) && !playerEntity.getDisplayName().getFormattedText().contains("NPC")) && remove.getValue()) {
                    mc.theWorld.removeEntity(playerEntity);
                }
            }
        }
    };

    private boolean isEntityBot(Entity entity) {
        double distance = entity.getDistanceSqToEntity(mc.thePlayer);
        if (!(entity instanceof EntityPlayer)) {
            return false;
        } else if (mc.getCurrentServerData() == null) {
            return false;
        } else {
            return mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel") && entity.getDisplayName().getFormattedText().startsWith("ยง") || !AntiBots.isOnTab(entity) && mc.thePlayer.ticksExisted > 100;
        }
    }
}
