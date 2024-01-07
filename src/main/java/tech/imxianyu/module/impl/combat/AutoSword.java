package tech.imxianyu.module.impl.combat;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;

import java.util.Optional;

public class AutoSword extends Module {

    @Handler
    public void onAttack(SendPacketEvent event) {
        if (ModuleManager.autoEat.isEnabled() && ModuleManager.autoEat.eating) {
            return;
        }

        if (ModuleManager.blockFly.isEnabled())
            return;


        if (event.getPacket() instanceof C02PacketUseEntity && ((C02PacketUseEntity) event.getPacket()).getAction() == C02PacketUseEntity.Action.ATTACK) {
            int bestSlot = -1;
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack != null) {
                    if (!(stack.getItem() instanceof ItemSword))
                        continue;

                    if (bestSlot == -1) {
                        bestSlot = i;
                    } else {
                        if (getSwordDamage(stack) > getSwordDamage(mc.thePlayer.inventory.getStackInSlot(bestSlot))) {
                            bestSlot = i;
                        } else if (getSwordDamage(stack) == getSwordDamage(mc.thePlayer.inventory.getStackInSlot(bestSlot)) && stack.getItemDamage() < mc.thePlayer.inventory.getStackInSlot(bestSlot).getItemDamage())
                            bestSlot = i;
                    }
                }
            }

            if (bestSlot != -1) {
                mc.thePlayer.inventory.currentItem = bestSlot;
            }
        }
    };

    public AutoSword() {
        super("Auto Sword", Category.COMBAT);
    }

    private double getSwordDamage(final ItemStack itemStack) {
        double damage = 0.0;
        final Optional<AttributeModifier> attributeModifier = itemStack.getAttributeModifiers().values().stream().findFirst();
        if (attributeModifier.isPresent()) {
            damage = attributeModifier.get().getAmount();
        }
        return damage += EnchantmentHelper.getModifierForCreature(itemStack, EnumCreatureAttribute.UNDEFINED);
    }
}
