package tech.imxianyu.module.impl.player;

import com.google.common.collect.Lists;
import net.minecraft.inventory.ContainerChest;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;

import tech.imxianyu.utils.timing.Timer;

import java.util.Collections;
import java.util.List;

/**
 * @author ImXianyu
 * @since 7/2/2023 3:54 PM
 */
public class ChestStealer extends Module {

    public ChestStealer() {
        super("Chest Stealer", Category.PLAYER);
    }

    private final NumberSetting<Integer> delay = new NumberSetting<>("Delay", 80, 0, 300, 10);
    public static BooleanSetting titleCheck = new BooleanSetting("Title Check", true);
    private final BooleanSetting reverse = new BooleanSetting("Reverse", false);

    private final Timer timer = new Timer();

    @Handler
    public void onUpdate(UpdateEvent event) {

        StringBuilder sb = new StringBuilder();

        sb.append("Delay: ").append(delay.getValue()).append(", ");
        if (titleCheck.getValue())
            sb.append("TC").append(", ");

        if (reverse.getValue())
            sb.append("Rev").append(", ");

        this.setSuffix(sb.substring(0, sb.toString().length() - 2));

        if (!event.isPre() || !(mc.thePlayer.openContainer instanceof ContainerChest))
            return;


        ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
        String chestName = chest.getLowerChestInventory().getName();
        if (titleCheck.getValue() && !((chestName.contains("Chest") && !chestName.equals("Ender Chest")) || chestName.equals("LOW")))
            return;

        List<Integer> slots = Lists.newArrayList();
        for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); i++) {
            if (chest.getLowerChestInventory().getStackInSlot(i) != null) {
                slots.add(i);
            }
        }

        if (reverse.getValue()) Collections.reverse(slots);

        for (int slot : slots) {
            if (delay.getValue() == 0 || timer.isDelayed( delay.getValue().longValue(), true)) {
                mc.playerController.windowClick(chest.windowId, slot, 0, 1, mc.thePlayer);
            }
        }

        if (slots.isEmpty() || this.isInventoryFull()) {
            mc.thePlayer.closeScreen();
        }

    };

    private boolean isInventoryFull() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getStack() == null) {
                return false;
            }
        }
        return true;
    }

}
