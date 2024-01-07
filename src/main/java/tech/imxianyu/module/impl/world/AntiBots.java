package tech.imxianyu.module.impl.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.world.antibots.Hypixel;

import java.util.*;

/**
 * @author ImXianyu
 * @since 2022/7/17 9:21
 */
public class AntiBots extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public AntiBots() {
        super("Anti Bots", Category.WORLD);
        super.addSubModules(new Hypixel());
    }

    public static boolean isOnTab(Entity entity) {
        Iterator<NetworkPlayerInfo> var2 = mc.getNetHandler().getPlayerInfoMap().iterator();

        NetworkPlayerInfo info;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            info = var2.next();
        } while (!info.getGameProfile().getName().equals(entity.getName()));

        return true;
    }

    public static boolean isDuplicateOnTab(Entity entity) {

        List<String> names = new ArrayList<>();

        for (NetworkPlayerInfo next : mc.getNetHandler().getPlayerInfoMap()) {
            try {
                names.add(next.getGameProfile().getName());
            } catch (Exception ignored) {

            }
        }

//        System.out.println(Arrays.toString(names.toArray()));

        if (names.stream().filter(s -> s.equals(entity.getName())).count() > 1)
            return true;

        return false;
    }

    public static boolean isBot(Entity entity) {
        if (ModuleManager.antiBots.isEnabled()) {

            if (isDuplicateOnTab(entity))
                return true;

            return false;

        } else {
            return false;
        }
    }
}
