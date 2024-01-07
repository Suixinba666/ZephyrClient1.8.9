package tech.imxianyu.module.impl.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.Formatting;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.management.FriendManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.world.AntiBots;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.math.MathUtils;

import java.awt.*;

public class NameTags extends Module {

    private static final String pro = "pro";
    private static final String sha = "sha";
    public NumberSetting<Float> scale = new NumberSetting<>("Scale", 1.0f, 0.1f, 1.0f, 0.1f);
    public BooleanSetting invisible = new BooleanSetting("Invisibles", false);
    public BooleanSetting renderArmor = new BooleanSetting("Render Armor", false);
    public BooleanSetting background = new BooleanSetting("Render Background", false);
    public BooleanSetting renderDistance = new BooleanSetting("Render Distance", false);
    public BooleanSetting renderHealth = new BooleanSetting("Render Health", false);
    @Handler
    public void onRender3D(Render3DEvent event) {
        for (EntityPlayer entity : mc.theWorld.playerEntities) {
            if (this.isValid(entity)) {
                final double yOffset = entity.isSneaking() ? -0.25 : 0.0;

                final double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - this.mc.getRenderManager().renderPosX;
                final double posY = (entity.lastTickPosY + yOffset) + ((entity.posY + yOffset) - (entity.lastTickPosY + yOffset)) * mc.timer.renderPartialTicks - this.mc.getRenderManager().renderPosY;
                final double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - this.mc.getRenderManager().renderPosZ;

                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);

                this.renderNameTag(entity, posX, posY, posZ, event.partialTicks);
            }
        }
    };

    public NameTags() {
        super("Name Tags", Category.RENDER);
    }

    public boolean isValid(EntityLivingBase entity) {

        if (entity.isInvisible() && !invisible.getValue())
            return false;

        return AntiBots.isOnTab(entity) || invisible.getValue();
    }

    private int getDisplayColour(EntityPlayer player) {
        int colour = new Color(0xFFFFFF).getRGB();

        if (player.isInvisible()) {
            colour = -1113785;
        }

        return colour;
    }

    private double interpolate(double previous, double current, float delta) {
        return previous + (current - previous) * (double) delta;
    }

    private void renderNameTag(EntityPlayer player, double x, double y, double z, float delta) {
        double tempY = y + 0.7D;

        Entity camera = this.mc.getRenderViewEntity();
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = this.interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = this.interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = this.interpolate(camera.prevPosZ, camera.posZ, delta);


        double distance = camera.getDistance(x + this.mc.getRenderManager().viewerPosX, y + this.mc.getRenderManager().viewerPosY, z + this.mc.getRenderManager().viewerPosZ);

        float width = (float) (FontManager.pf18.getStringWidth(this.getDisplayName(player)) / 2);

        double scale1 = (double) (0.004F * scale.getValue()) * distance;

        if (scale1 < 0.01)
            scale1 = 0.01;

        GlStateManager.pushMatrix();

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0F, -1500000.0F);

        GlStateManager.disableLighting();

        GlStateManager.translate((float) x, (float) tempY + 1.4F, (float) z);
        boolean flag = this.mc.gameSettings.thirdPersonView == 2;
        GlStateManager.rotate(-Perspective.getCameraYaw() + (flag ? 180 : 0), 0.0F, 1.0F, 0.0F);
        float var10001 = flag ? -1.0F : 1.0F;
        GlStateManager.rotate(Perspective.getCameraPitch(), var10001, 0.0F, 0.0F);
        GlStateManager.scale(-scale1, -scale1, scale1);
        GlStateManager.resetColor();
        if (background.getValue()) {
            RenderSystem.drawRect(-width - 2, (float) -(FontManager.pf18.getHeight() + 2), width + 2.0F, 2.0F, FriendManager.isFriend(player) ? RenderSystem.hexColor(0, 255, 0, 160) : RenderSystem.hexColor(25, 25, 25, 160));
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GlStateManager.translate(0, 0, -0.1);
        //Fix Color Glitch
        GL11.glDepthMask(false);
        FontManager.pf18.drawStringWithShadow(this.getDisplayName(player), -width, -(FontManager.pf18.getHeight()), this.getDisplayColour(player));
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDepthMask(true);
        GlStateManager.translate(0, 0, 0.1);

        if (renderArmor.getValue()) {
            this.renderArmor(player);

            //No Paper item
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
        }

        GlStateManager.disableLighting();

        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;

        GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
        GlStateManager.disablePolygonOffset();

        GlStateManager.popMatrix();
    }

    private void renderArmor(EntityPlayer player) {
        int xOffset = 0;

        int index;
        ItemStack stack;
        for (index = 3; index >= 0; --index) {
            stack = player.inventory.armorInventory[index];
            if (stack != null) {
                xOffset -= 8;
            }
        }

        if (player.getCurrentEquippedItem() != null) {
            xOffset -= 8;
            ItemStack var27 = player.getCurrentEquippedItem().copy();
            if (var27.hasEffect() && (var27.getItem() instanceof ItemTool || var27.getItem() instanceof ItemArmor)) {
                var27.stackSize = 1;
            }

            this.renderItemStack(var27, xOffset, -26);
            xOffset += 16;
        }

        for (index = 3; index >= 0; --index) {
            stack = player.inventory.armorInventory[index];
            if (stack != null) {
                ItemStack armourStack = stack.copy();
                if (armourStack.hasEffect() && (armourStack.getItem() instanceof ItemTool || armourStack.getItem() instanceof ItemArmor)) {
                    armourStack.stackSize = 1;
                }

                this.renderItemStack(armourStack, xOffset, -26);
                xOffset += 16;
            }
        }
    }

    private void renderItemStack(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();

        GlStateManager.disableAlpha();
        this.mc.getRenderItem().zLevel = -150.0F;

        GlStateManager.disableCull();
        RenderHelper.enableGUIStandardItemLighting();

        this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        this.mc.getRenderItem().renderItemOverlays(this.mc.fontRendererObj, stack, x, y);

        RenderHelper.disableStandardItemLighting();

        GlStateManager.enableCull();

        this.mc.getRenderItem().zLevel = 0;

        GlStateManager.disableBlend();

        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        GlStateManager.disableDepth();
        GlStateManager.disableLighting();

        this.renderEnchantmentText(stack, x, y);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();

        GlStateManager.scale(2.0F, 2.0F, 2.0F);

        GlStateManager.enableAlpha();

        GlStateManager.popMatrix();
    }

    private void renderEnchantmentText(ItemStack stack, int x, int y) {
        try {
            int enchantmentY = y - 24;
            int color = new Color(0xFFFFFF).getRGB();
            if (stack.getItem() instanceof ItemArmor) {
                int protection = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
                int projectileProtection = EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack);
                int blastProtection = EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack);
                int fireProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack);
                int thornsLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
                int featherFallingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, stack);
                int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);

                if (protection > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(pro + protection, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (unbreakingLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.unbreaking.getName().substring(0, 3) + unbreakingLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (projectileProtection > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.projectileProtection.getName().substring(0, 3) + projectileProtection, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (blastProtection > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.blastProtection.getName().substring(0, 3) + blastProtection, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (fireProtectionLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.fireAspect.getName().substring(0, 3) + fireProtectionLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (thornsLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.thorns.getName().substring(0, 3) + thornsLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (featherFallingLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.featherFalling.getName().substring(0, 3) + featherFallingLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                /*
                boolean dura = false;
                if(dura && stack.getMaxDamage() - stack.getItemDamage() < stack.getMaxDamage()) {
                    this.mc.fontRendererObj.drawStringWithShadow(stack.getMaxDamage() - stack.getItemDamage() + "", x * 2, enchantmentY + 2, -26215);
                    enchantmentY += 8;
                }*/
            }

            if (stack.getItem() instanceof ItemBow) {
                int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
                int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
                int flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);

                if (powerLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.power.getName().substring(0, 3) + powerLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (punchLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.punch.getName().substring(0, 3) + punchLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (flameLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.flame.getName().substring(0, 3) + flameLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }
            }

            if (stack.getItem() instanceof ItemPickaxe) {
                int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
                int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack);

                if (efficiencyLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.efficiency.getName().substring(0, 3) + efficiencyLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (fortuneLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.fortune.getName().substring(0, 3) + fortuneLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }
            }

            if (stack.getItem() instanceof ItemAxe) {
                int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
                int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
                int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
                if (sharpnessLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.sharpness.getName().substring(0, 3) + sharpnessLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (fireAspect > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.fireAspect.getName().substring(0, 3) + fireAspect, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (efficiencyLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.efficiency.getName().substring(0, 3) + efficiencyLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }
            }

            if (stack.getItem() instanceof ItemSword) {
                int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
                int knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
                int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
                if (sharpnessLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(sha + sharpnessLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (knockbackLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.knockback.getName().substring(0, 3) + knockbackLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }

                if (fireAspectLevel > 0) {
                    this.mc.fontRendererObj.drawStringWithShadow(Enchantment.fireAspect.getName().substring(0, 3) + fireAspectLevel, x * 2, enchantmentY, color);
                    enchantmentY += 8;
                }
            }

           /* if(stack.getItem() == Items.golden_apple && stack.hasEffect()) {
                this.mc.fontRendererObj.drawStringWithShadowWithShadow("god", (float)(x * 2), (float)enchantmentY, -3977919);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDisplayName(EntityLivingBase entity) {
        String drawTag = entity.getDisplayName().getFormattedText();

        Formatting color;

        final double health = MathUtils.roundToPlace(entity.getHealth() / 2.00, 2);

        if (health >= 6.0) {
            color = Formatting.GREEN;
        } else if (health >= 2.0) {
            color = Formatting.YELLOW;
        } else {
            color = Formatting.RED;
        }

        String clientTag = "";

        drawTag = (renderDistance.getValue() ? Formatting.GRAY + "[" + (int) entity.getDistanceToEntity(this.mc.thePlayer) + "m] " : "") + Formatting.RESET + clientTag + Formatting.RESET + (FriendManager.isFriend(entity) ? Formatting.GREEN + "[F] " : Formatting.GRAY) + drawTag + " " + (renderHealth.getValue() ? String.valueOf(color) + health : "");

        return drawTag;
    }
}
