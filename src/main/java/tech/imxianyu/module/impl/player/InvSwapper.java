package tech.imxianyu.module.impl.player;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.player.InvUtils;
import tech.imxianyu.utils.timing.Timer;

import java.util.Arrays;
import java.util.List;

/**
 * @author ImXianyu
 * @since 7/2/2023 4:25 PM
 */
public class InvSwapper extends Module {

    public InvSwapper() {
        super("Inventory Swapper", Category.PLAYER);
    }
    private final NumberSetting<Long> delay = new NumberSetting<>("Delay", 80L, 0L,
            1000L, 10L);
    public ModeSetting<ItemType> slot1 = new ModeSetting<>("Slot 1", ItemType.DontSwap);
    public ModeSetting<ItemType> slot2 = new ModeSetting<>("Slot 2", ItemType.DontSwap);
    public ModeSetting<ItemType> slot3 = new ModeSetting<>("Slot 3", ItemType.DontSwap);
    public ModeSetting<ItemType> slot4 = new ModeSetting<>("Slot 4", ItemType.DontSwap);
    public ModeSetting<ItemType> slot5 = new ModeSetting<>("Slot 5", ItemType.DontSwap);

    public final Timer timer = new Timer();

    public enum ItemType {
        Sword,
        Block,
        Bow,
        Axe,
        Pickaxe,
        GApple,
        DontSwap
    }

    @Handler
    public void onTick(TickEvent event) {


        if (!timer.isDelayed(delay.getValue()))
            return;

        List<ModeSetting<ItemType>> slots = Arrays.asList(slot1, slot2, slot3, slot4, slot5);

        StringBuilder sb = new StringBuilder();

        for (ModeSetting<ItemType> slot : slots) {
            String name = slot.getValue().name();
            if (slot.getValue() == ItemType.DontSwap)
                name = "None";

            sb.append(name).append(", ");
        }

        this.setSuffix(sb.substring(0, sb.toString().length() - 2));

        timer.reset();

        this.trySwap(0, slot1.getValue());
        this.trySwap(1, slot2.getValue());
        this.trySwap(2, slot3.getValue());
        this.trySwap(3, slot4.getValue());
        this.trySwap(4, slot5.getValue());
    };

    private String getSlotAbbr(ItemType type) {
        return type.name().substring(0, 1);
    }

    private void trySwap(int slot, ItemType type) {
        if (type == ItemType.DontSwap)
            return;

        int swapSlot = slot;

        switch (type) {
            case Sword: {

                for (int i = 0; i < InvUtils.getInventoryAndHotBarContent().size(); i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null) {
                        if (!(stack.getItem() instanceof ItemSword))
                            continue;

                        if (mc.thePlayer.inventory.getStackInSlot(swapSlot) == null) {
                            swapSlot = i;
                        } else {
                            if (getSwordAttackDamage(stack) > getSwordAttackDamage(mc.thePlayer.inventory.getStackInSlot(swapSlot))) {
                                swapSlot = i;
                            } else if (getSwordAttackDamage(stack) == getSwordAttackDamage(mc.thePlayer.inventory.getStackInSlot(swapSlot)) && stack.getItemDamage() < mc.thePlayer.inventory.getStackInSlot(swapSlot).getItemDamage())
                                swapSlot = i;
                        }
                    }
                }

                break;
            }

            case Block: {

                for (int i = 0; i < InvUtils.getInventoryAndHotBarContent().size(); i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null) {
                        if (!(stack.getItem() instanceof ItemBlock))
                            continue;

                        if (mc.thePlayer.inventory.getStackInSlot(swapSlot) == null) {
                            swapSlot = i;
                        }/* else {
                            if (stack.stackSize > mc.thePlayer.inventory.getStackInSlot(swapSlot).stackSize) {
                                swapSlot = i;
                            }
                        }*/
                    }
                }

                break;
            }

            case Axe: {
                for (int i = 0; i < InvUtils.getInventoryAndHotBarContent().size(); i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null) {
                        if (!(stack.getItem() instanceof ItemAxe))
                            continue;

                        if (mc.thePlayer.inventory.getStackInSlot(swapSlot) == null) {
                            swapSlot = i;
                        }

                        if (InvUtils.isBestAxe(stack))
                            swapSlot = i;
                    }
                }
                break;
            }

            case Bow: {
                for (int i = 0; i < InvUtils.getInventoryAndHotBarContent().size(); i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null) {
                        if (!(stack.getItem() instanceof ItemBow))
                            continue;

                        if (mc.thePlayer.inventory.getStackInSlot(swapSlot) == null) {
                            swapSlot = i;
                        }

                        if (isBestBow(stack))
                            swapSlot = i;
                    }
                }
                break;
            }

            case GApple: {
                for (int i = 0; i < InvUtils.getInventoryAndHotBarContent().size(); i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null) {
                        if (!(stack.getItem() == Items.golden_apple))
                            continue;

                        if (mc.thePlayer.inventory.getStackInSlot(swapSlot) == null) {
                            swapSlot = i;
                        } else {
                            if (stack.stackSize > mc.thePlayer.inventory.getStackInSlot(swapSlot).stackSize) {
                                swapSlot = i;
                            }
                        }
                    }
                }
                break;
            }

            case Pickaxe: {
                for (int i = 0; i < InvUtils.getInventoryAndHotBarContent().size(); i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null) {
                        if (!(stack.getItem() instanceof ItemPickaxe))
                            continue;

                        if (mc.thePlayer.inventory.getStackInSlot(swapSlot) == null) {
                            swapSlot = i;
                        }

                        if (InvUtils.isBestPickaxe(stack))
                            swapSlot = i;
                    }
                }
                break;
            }
        }

        if (swapSlot != slot) {
//            mc.thePlayer.addChatMessage("Type: " + type.name() + ", Slot: " + swapSlot + " -> " + slot);

            if (swapSlot <= 8) {
                InvUtils.swap(swapSlot + 36, slot);
            } else {
                InvUtils.swap(swapSlot, slot);

            }

        }

    }

    private boolean isBestBow(ItemStack input) {
        for (ItemStack itemStack : InvUtils.getAllInventoryContent()) {
            if (itemStack == null)
                continue;

            if (!(itemStack.getItem() instanceof ItemBow))
                continue;

            if (itemStack == input)
                continue;

            if (getBowAttackDamage(itemStack) >= getBowAttackDamage(input))
                return false;
        }
        return true;
    }

    private double getBowAttackDamage(ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItem() instanceof ItemBow))
            return 0;

        return EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemStack)
                + (EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemStack) * 0.1)
                + (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemStack) * 0.1);
    }

    private double getSwordAttackDamage(final ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItem() instanceof ItemSword))
            return 0;

        ItemSword sword = (ItemSword) itemStack.getItem();

        return EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack)
                + sword.attackDamage;
    }



}
