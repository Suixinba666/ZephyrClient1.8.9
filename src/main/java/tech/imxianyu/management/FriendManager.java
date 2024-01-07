package tech.imxianyu.management;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Formatting;
import org.lwjglx.input.Mouse;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.interfaces.AbstractManager;

import java.util.List;

public class FriendManager extends AbstractManager {

    public static final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private static final List<String> friends = Lists.newArrayList();
    boolean previousMouse = false;
    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (!event.isPre())
            return;

        if (!Mouse.isButtonDown(2)) {
            previousMouse = false;
        }

        if (mc.objectMouseOver == null)
            return;

        Entity entity = mc.objectMouseOver.entityHit;
        if (entity == null)
            return;
        if (entity instanceof EntityPlayer && Mouse.isButtonDown(2) && !previousMouse) {
            previousMouse = true;

            synchronized (friends) {

                if (!friends.contains(entity.getName().toLowerCase())) {
                    friends.add(entity.getName().toLowerCase());
                    mc.thePlayer.addChatComponentMessage(new ChatComponentText(Formatting.GREEN + "Added friend: " + Formatting.GOLD + entity.getName()));
                } else {
                    friends.remove(entity.getName().toLowerCase());
                    mc.thePlayer.addChatComponentMessage(new ChatComponentText(Formatting.RED + "Removed friend: " + Formatting.GOLD + entity.getName()));
                }
            }
        }
    };

    public static boolean isFriend(Entity entity) {
        if (entity == mc.thePlayer) {
            return true;
        }

        if (!ModuleManager.friends.isEnabled())
            return false;

        if (!(entity instanceof EntityPlayer))
            return false;

        synchronized (friends) {
            return friends.contains(entity.getName().toLowerCase());
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }
}
