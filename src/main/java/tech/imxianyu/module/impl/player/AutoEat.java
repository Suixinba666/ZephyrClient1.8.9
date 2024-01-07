package tech.imxianyu.module.impl.player;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.player.InvUtils;
import tech.imxianyu.utils.timing.Timer;

public class AutoEat extends Module {

    public NumberSetting<Integer> healthToEat = new NumberSetting<Integer>("Health To Eat (Percent)", 80, 1, 100, 1) {
        @Override
        public String getStringForRender() {
            return this.getValue() + "%";
        }
    };
    public static BooleanSetting gApple = new BooleanSetting("Eat GApple", false);
    public static BooleanSetting head = new BooleanSetting("Eat Golden Head", false);
    public static BooleanSetting ragePotato = new BooleanSetting("Eat Rage Potato", false);
    public boolean eating = false;

    int bestSlot, oldSlot;
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        this.setSuffix(healthToEat.getStringForRender());


        if (this.oldSlot == -1) {
            if (mc.thePlayer.capabilities.isCreativeMode || !(((mc.thePlayer.getHealth()) / mc.thePlayer.getMaxHealth() * 100) < healthToEat.getValue()) || !timer.isDelayed(500)) {
                return;
            }

            this.bestSlot = -1;
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack == null) continue;

                if (stack.getItem() instanceof ItemFood) {
                    if (gApple.getValue() && stack.getItem() == Items.golden_apple)
                        this.bestSlot = i;
                    else if (ragePotato.getValue() && stack.getItem() == Items.baked_potato)
                        this.bestSlot = i;
                }

                if (head.getValue() && stack.getItem() == Items.skull)
                    this.bestSlot = i;

            }
            if (this.bestSlot == -1) {
                return;
            }
            mc.thePlayer.addChatMessage(new ChatComponentText("[AutoEat] Eat, Item: " + mc.thePlayer.inventory.getStackInSlot(bestSlot).getDisplayName()));
            this.oldSlot = mc.thePlayer.inventory.currentItem;
        } else {
            eating = true;
            if (mc.thePlayer.capabilities.isCreativeMode || !(((mc.thePlayer.getHealth()) / mc.thePlayer.getMaxHealth() * 100) < healthToEat.getValue())) {
                this.stop();
                return;
            }
            ItemStack bestSlot = mc.thePlayer.inventory.getStackInSlot(this.bestSlot);
            if (bestSlot == null || !(bestSlot.getItem() instanceof ItemFood) && !(bestSlot.getItem() instanceof ItemSkull)) {
                this.stop();
                return;
            }
            mc.thePlayer.inventory.currentItem = this.bestSlot;
            mc.gameSettings.keyBindUseItem.pressed = true;
        }


    };

    Timer timer = new Timer();

    public void stop() {
        eating = false;
        mc.gameSettings.keyBindUseItem.pressed = false;
        if (this.oldSlot != -1) {
            mc.thePlayer.inventory.currentItem = this.oldSlot;
            this.oldSlot = -1;
        }
        this.timer.reset();
    }

    public AutoEat() {
        super("Auto Eat", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        this.stop();
    }
}
