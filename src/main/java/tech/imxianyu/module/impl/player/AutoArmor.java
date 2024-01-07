package tech.imxianyu.module.impl.player;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.utils.player.InvUtils;
import tech.imxianyu.utils.timing.Timer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * @author ImXianyu
 * @since 7/2/2023 3:57 PM
 */
public class AutoArmor extends Module {

    public AutoArmor() {
        super("Auto Armor", Category.PLAYER);
    }

    private final NumberSetting<Integer> delay = new NumberSetting<>("Delay", 150, 0, 300, 10);
    private final BooleanSetting onlyWhileNotMoving = new BooleanSetting("Only while not moving", true);
    private final BooleanSetting drop = new BooleanSetting("Drop Armor", false);
    private final BooleanSetting invOnly = new BooleanSetting("Inventory only", true);
    private final Timer timer = new Timer();

    @Handler
    public void onUpdate(UpdateEvent e) {
        this.setSuffix("Delay: " + delay.getValue());
        if (!e.isPre()) return;
        if ((invOnly.getValue() && !(mc.currentScreen instanceof GuiInventory)) || (onlyWhileNotMoving.getValue() && PlayerUtils.isMoving2())) {
            return;
        }
        if (mc.thePlayer.openContainer instanceof ContainerChest) {
            // so it doesn't put on armor immediately after closing a chest
            timer.reset();
        }
        if (timer.isDelayed(delay.getValue().longValue())) {
            for (int armorSlot = 5; armorSlot < 9; armorSlot++) {
                if (equipBest(armorSlot)) {
                    timer.reset();
                    break;
                }
            }
        }
    };

    private boolean equipBest(int armorSlot) {
        int equipSlot = -1, currProt = -1, currDmg = Integer.MAX_VALUE;
        ItemArmor currItem = null;
        ItemStack slotStack = mc.thePlayer.inventoryContainer.getSlot(armorSlot).getStack();
        if (slotStack != null && slotStack.getItem() instanceof ItemArmor) {
            currItem = (ItemArmor) slotStack.getItem();
            currProt = currItem.damageReduceAmount
                    + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, mc.thePlayer.inventoryContainer.getSlot(armorSlot).getStack());
            currDmg = slotStack.getItemDamage();
        }

        for (int i = 0; i < InvUtils.getInventoryAndHotBarContent().size(); i++) {
            ItemStack is = mc.thePlayer.inventory.getStackInSlot(i);
            if (is != null && is.getItem() instanceof ItemArmor) {
                int prot = ((ItemArmor) is.getItem()).damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, is);
                int dmg = is.getItemDamage();

/*                if (is.getItem() == currItem) {
                    System.out.println(currDmg + ", " + dmg);
                }*/

                if ((currItem == null || (currProt < prot || (currProt == prot && currDmg > dmg))) && isValidPiece(armorSlot, (ItemArmor) is.getItem())) {
//                    System.out.println(true);
                    currItem = (ItemArmor) is.getItem();
                    equipSlot = i;
                    currProt = prot;
                    currDmg = dmg;
                }
            }
        }
        // equip best piece (if there is a better one)
        if (equipSlot != -1) {
            if (equipSlot <= 8)
                equipSlot += 36;
            if (slotStack != null) {
                if (drop.getValue())
                    InvUtils.drop(armorSlot);
                else
                    InvUtils.swap(armorSlot, equipSlot);

            } else {
                InvUtils.click(equipSlot, 0, true);
            }
            return true;
        }
        return false;
    }

    private boolean isValidPiece(int armorSlot, ItemArmor item) {
        String unlocalizedName = item.getUnlocalizedName();
        return armorSlot == 5 && unlocalizedName.startsWith("item.helmet")
                || armorSlot == 6 && unlocalizedName.startsWith("item.chestplate")
                || armorSlot == 7 && unlocalizedName.startsWith("item.leggings")
                || armorSlot == 8 && unlocalizedName.startsWith("item.boots");
    }



}
