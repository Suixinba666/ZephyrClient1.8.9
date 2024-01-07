package tech.imxianyu.module.impl.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.player.InvUtils;
import tech.imxianyu.utils.timing.Timer;

public class InvCleaner extends Module {
	private final NumberSetting<Long> delay = new NumberSetting<>("Delay", 80L, 0L,
			1000L, 10L);
	public BooleanSetting toggle = new BooleanSetting("Auto Toggle", false);
	public BooleanSetting keepTools = new BooleanSetting("Keep Tools", false);
	public BooleanSetting keepArmor = new BooleanSetting("Keep Armor", false);
	public BooleanSetting keepBow = new BooleanSetting("Keep Bow", false);
	public BooleanSetting keepBucket = new BooleanSetting("Keep Bucket", false);
	public BooleanSetting keepArrow = new BooleanSetting("Keep Arrows", false);
	public BooleanSetting inInv = new BooleanSetting("OnlyInv", false);

	public Timer delayTimer = new Timer();

	private double handItemAttackValue;
	private int currentSlot = 9;

	public InvCleaner() {
		super("Inventory Cleaner", Category.PLAYER);
	}

	public boolean isItemNotUseful(int slot) {
		ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

		if (itemStack == null)
			return false;

		if (itemStack.getItem() == Items.stick)
			return true;

		if (itemStack.getItem() == Items.egg)
			return true;

		if (itemStack.getItem() == Items.bone)
			return true;

		if (itemStack.getItem() == Items.bowl)
			return true;

		if (itemStack.getItem() == Items.glass_bottle)
			return true;

		if (itemStack.getItem() == Items.string)
			return true;

		if (itemStack.getItem() == Items.flint_and_steel && getItemAmount(Items.flint_and_steel) > 1)
			return true;

		if (itemStack.getItem() == Items.compass && getItemAmount(Items.compass) > 1)
			return true;

		if (itemStack.getItem() == Items.feather)
			return true;

		if (itemStack.getItem() == Items.fishing_rod)
			return true;

		// buckets
		if (itemStack.getItem() == Items.bucket && !keepBucket.getValue())
			return true;

		if (itemStack.getItem() == Items.lava_bucket && !keepBucket.getValue())
			return true;

		if (itemStack.getItem() == Items.water_bucket && !keepBucket.getValue())
			return true;

		if (itemStack.getItem() == Items.milk_bucket && !keepBucket.getValue())
			return true;

		// arrow
		if (itemStack.getItem() == Items.arrow && !keepArrow.getValue())
			return true;

		if (itemStack.getItem() == Items.snowball)
			return true;

		if (itemStack.getItem() == Items.fish)
			return true;

		if (itemStack.getItem() == Items.experience_bottle)
			return true;

		// tools
		if (itemStack.getItem() instanceof ItemTool && (!keepTools.getValue() || !isBestTool(itemStack)))
			return true;

		// sword
		if (itemStack.getItem() instanceof ItemSword && (!keepTools.getValue() || !isBestSword(itemStack)))
			return true;

		// armour
		if (itemStack.getItem() instanceof ItemArmor && (!keepArmor.getValue() || !isBestArmor(itemStack)))
			return true;

		// bow
		if (itemStack.getItem() instanceof ItemBow && (!keepBow.getValue() || !isBestBow(itemStack)))
			return true;

		if (itemStack.getItem() instanceof ItemBlock && (getAllBlocksAmount() > 256 || isBlockNotUseful(((ItemBlock) itemStack.getItem()).getBlock())))
			return true;

		if (itemStack.getItem().getUnlocalizedName().contains("potion")) {
			return isBadPotion(itemStack) || getItemAmount(Items.potionitem) > 3;
		}

		return false;
	}

	private boolean isBlockNotUseful(Block block) {

		if (block instanceof BlockTNT)
			return true;

		return false;
	}

	private int getAllBlocksAmount() {
		int result = 0;

		for (ItemStack stack : InvUtils.getInventoryAndHotBarContent()) {
			if (stack != null && stack.getItem() instanceof ItemBlock)
				result += stack.stackSize;
		}

		return result;
	}

	private int getItemAmount(Item shit) {
		int result = 0;

		for (ItemStack stack : InvUtils.getInventoryAndHotBarContent()) {
			if (stack != null && stack.getItem() == shit)
				result += stack.stackSize;
		}

		return result;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		currentSlot = 9; // EXCEPT ARMOR SLOT

		if (mc.thePlayer == null || mc.theWorld == null)
			return;
		handItemAttackValue = getSwordAttackDamage(mc.thePlayer.getHeldItem());
	}

	@Handler
    public void onTick(TickEvent event) {
		if (mc.currentScreen instanceof GuiChest || !event.isPre())
			return;

		if (currentSlot >= 45) {
			currentSlot = 9;
			if (toggle.getValue()) {
				this.toggle();
				return;
			}
		}

		if (!inInv.getValue() || mc.currentScreen instanceof GuiInventory) {
			handItemAttackValue = getSwordAttackDamage(mc.thePlayer.getHeldItem());
			final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(currentSlot).getStack();
			if (delayTimer.isDelayed(delay.getValue())) {
				if (isItemNotUseful(currentSlot) && getSwordAttackDamage(itemStack) <= handItemAttackValue && itemStack != mc.thePlayer.getHeldItem()) {
					InvUtils.drop(currentSlot);
					delayTimer.reset();
				}
				currentSlot++;
			}
		}
	};

	private boolean isBestTool(ItemStack input) {
		for (ItemStack itemStack : InvUtils.getAllInventoryContent()) {
			if (itemStack == null)
				continue;

			if (!(itemStack.getItem() instanceof ItemTool))
				continue;

			if (itemStack == input)
				continue;

			if (itemStack.getItem() instanceof ItemPickaxe && !(input.getItem() instanceof ItemPickaxe))
				continue;

			if (itemStack.getItem() instanceof ItemAxe && !(input.getItem() instanceof ItemAxe))
				continue;

			if (itemStack.getItem() instanceof ItemSpade && !(input.getItem() instanceof ItemSpade))
				continue;

			if (getToolEfficiency(itemStack) >= getToolEfficiency(input))
				return false;
		}
		return true;
	}
	private boolean isBestSword(ItemStack input) {
		for (ItemStack itemStack : InvUtils.getAllInventoryContent()) {
			if (itemStack == null)
				continue;

			if (!(itemStack.getItem() instanceof ItemSword))
				continue;

			if (itemStack == input)
				continue;

			if (getSwordAttackDamage(itemStack) >= getSwordAttackDamage(input))
				return false;

			if (getSwordAttackDamage(itemStack) == getSwordAttackDamage(input) && input.getItemDamage() > itemStack.getItemDamage())
				return false;
		}
		return true;
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

	private boolean isBestArmor(ItemStack input) {
		for (ItemStack itemStack : InvUtils.getAllInventoryContent()) {
			if (itemStack == null)
				continue;

			if (!(itemStack.getItem() instanceof ItemArmor))
				continue;

			if (itemStack == input)
				continue;

			if (((ItemArmor) itemStack.getItem()).armorType != ((ItemArmor) input.getItem()).armorType)
				continue;

			if (InvUtils.getArmorScore(itemStack) >= InvUtils.getArmorScore(input))
				return false;
		}
		return true;
	}

	private boolean isBadPotion(final ItemStack stack) {
		if (stack != null && stack.getItem() instanceof ItemPotion) {
			final ItemPotion potion = (ItemPotion) stack.getItem();
			for (final PotionEffect o : potion.getEffects(stack)) {
				if (o.getPotionID() == Potion.poison.getId() || o.getPotionID() == Potion.moveSlowdown.getId()
						|| o.getPotionID() == Potion.harm.getId()) {
					return true;
				}
			}
		}
		return false;
	}

	private double getSwordAttackDamage(final ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof ItemSword))
			return 0;

		ItemSword sword = (ItemSword) itemStack.getItem();

		return EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack)
				+ sword.attackDamage;
	}

	private double getBowAttackDamage(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof ItemBow))
			return 0;

		return EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemStack)
				+ (EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemStack) * 0.1)
				+ (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemStack) * 0.1);
	}

	private double getToolEfficiency(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof ItemTool))
			return 0;

		ItemTool sword = (ItemTool) itemStack.getItem();

		return EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack)
				+ sword.efficiencyOnProperMaterial;
	}

}