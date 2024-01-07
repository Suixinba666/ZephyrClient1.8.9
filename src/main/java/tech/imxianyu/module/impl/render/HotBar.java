package tech.imxianyu.module.impl.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import net.optifine.CustomColors;
import net.optifine.CustomItems;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.utils.timing.Timer;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Optional;

/**
 * @author ImXianyu
 * @since 6/27/2023 7:08 PM
 */
public class HotBar extends Module {

    public double damageDealt = 0;
    public Timer damageTimer = new Timer();
    double stackX;
    double hurtAnimPerc = 1.0;
    int hurtAnimAlpha = 0;
    double lastHealth;
    DecimalFormat df = new DecimalFormat("##.##");
    private double targetHealthWidth = 0, targetHealthPercent;
    private double targetFoodWidth = 0, targetFoodPercent;
    private double targetArmorWidth = 0, targetArmorPercent;
    private double targetExpWidth = 0;
    private double targetAbspWidth = 0;
    private double targetHealthWidthLast = 0;

    Framebuffer current;
    double[] scales = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    @Handler
    public void onRender2D(Render2DEvent event) {

        double hotbarHeight = 26;

        double hotbarItemSpacing = 4;
        double hotbarWidth = hotbarItemSpacing * 8 + hotbarHeight * 9;

        double hotbarItemWidth = ((hotbarWidth - (hotbarItemSpacing * 8)) / 9);

        double hotbarX = RenderSystem.getWidth() / 2 - hotbarWidth / 2;
        double hotbarY = RenderSystem.getHeight() - hotbarHeight - 10;

        double offsetX = hotbarX, offsetY = hotbarY;

        if (stackX < hotbarX + hotbarItemWidth / 2)
            stackX = hotbarX + (hotbarItemWidth + hotbarItemSpacing) * mc.thePlayer.inventory.currentItem + hotbarItemWidth / 2;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];

//            Rect.draw(offsetX, hotbarY, hotbarItemWidth, hotbarHeight, ColorUtils.getColor(ColorUtils.ColorType.Base), Rect.RectType.EXPAND);
            Image.draw(new ResourceLocation("Zephyr/textures/shadow/textshadow.png"), offsetX, hotbarY - 7, hotbarItemWidth, hotbarHeight * 1.5, Image.Type.Normal);
            Image.draw(new ResourceLocation("Zephyr/textures/shadow/textshadow.png"), offsetX, hotbarY - 7, hotbarItemWidth, hotbarHeight * 1.5, Image.Type.Normal);

            if (stack != null) {
                this.renderItem(stack, (int) offsetX + 4, (int) offsetY + 5, scales[i] * 0.25);

                if (stack.stackSize != 1) {
                    FontManager.pf12.drawString(String.valueOf(stack.stackSize), offsetX + 20, offsetY + 20, RenderSystem.reAlpha(-1, (float) (scales[i] * 0.5f + 0.25f)));
                }

                if (stack.isItemDamaged()) {
                    double maxDamage = stack.getMaxDamage();
                    double damageBarWidth = 16;
                    double damageWidth = stack.getItemDamage() / maxDamage * damageBarWidth;

                    Rect.draw(offsetX + hotbarItemWidth / 2.0 - damageBarWidth / 2.0, offsetY + hotbarHeight - 1, damageBarWidth, 1.5, RenderSystem.hexColor(0, 0, 0), Rect.RectType.EXPAND);
                    Rect.draw(offsetX + hotbarItemWidth / 2.0 - damageBarWidth / 2.0, offsetY + hotbarHeight - 1, damageBarWidth - damageWidth, 1.5, RenderSystem.hexColor(0, 255, 0), Rect.RectType.EXPAND);
                }
            }

            if (i == mc.thePlayer.inventory.currentItem) {
                scales[i] = AnimationSystem.interpolate(scales[i] * 100, 100, 0.2) / 100.0;
                stackX = AnimationSystem.interpolate(stackX, offsetX + hotbarItemWidth / 2, 0.4);
//                double hintWidth = hotbarItemWidth - 8, hintHeight = 2;
//                RoundedRect.drawRound(stackX - hintWidth / 2, offsetY + hotbarHeight + 2, hintWidth, hintHeight, 1f, Color.WHITE);

                ShaderUtils.doRoundedBlurAndBloom(stackX - hotbarItemWidth * 0.5 - 1, offsetY, hotbarItemWidth, hotbarItemWidth, 5);


                if (stack != null) {
                    FontManager.pf16.drawCenteredStringWithBetterShadow(stack.getDisplayName(), stackX, offsetY - 9 - FontManager.pf16.getHeight(), new Color(255, 255, 255, 200).getRGB());
                    String itemDesc = "";
                    Item item = stack.getItem();
                    if (item instanceof ItemSword) {
                        itemDesc = "+" + this.getSwordDamage(stack) + " Attack Damage";
                    } else {

                        CreativeTabs creativeTab = item.getCreativeTab();

                        if (creativeTab != null) {
                            String label = creativeTab.getTabLabel();
                            label = label.substring(0, 1).toUpperCase() + label.substring(1);
                            itemDesc = label;
                        }
                    }

                    FontManager.pf12.drawCenteredStringWithBetterShadow(itemDesc, stackX, offsetY - 7.5, /*RenderSystem.hexColor(64, 64, 64)*/new Color(255, 255, 255, 70).getRGB());
                }
            } else {
                scales[i] = AnimationSystem.interpolate(scales[i] * 100, 0, 0.2) / 100.0;
                if (scales[i] <= 0.01)
                    scales[i] = 0;
            }


            offsetX += hotbarItemWidth + hotbarItemSpacing;
        }


        if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SURVIVAL || mc.playerController.getCurrentGameType() == WorldSettings.GameType.ADVENTURE) {

            double barsY = hotbarY - 12;
            this.drawHealthBar(hotbarX, barsY, hotbarWidth);
            this.drawFoodBar(hotbarX, barsY, hotbarWidth);
            this.drawArmorBar(hotbarX, barsY, hotbarWidth);
            this.drawExpBar(hotbarX, barsY, hotbarWidth);

        }

    }

    public HotBar() {
        super("Hot Bar", Category.RENDER);
        super.setDescription("Replaces ur hot bar.");
    }

    private void drawExpBar(double hotbarX, double hotbarY, double hotbarWidth) {
        double expBarHeight = 10;

        int y = 16;

        double expBarWidth = mc.thePlayer.experience * hotbarWidth;

        targetExpWidth = AnimationSystem.interpolate(targetExpWidth, expBarWidth, 0.4f);

        if (Double.isNaN(targetExpWidth))
            targetExpWidth = expBarWidth;

        ShaderUtils.doRectBlurAndBloom(hotbarX, hotbarY - y, hotbarWidth, expBarHeight);
        Rect.draw(hotbarX, hotbarY - y, targetExpWidth, expBarHeight, 0xff0090ff, Rect.RectType.EXPAND);

        int k1 = RenderSystem.hexColor(128, 255, 32);

        if (Config.isCustomColors())
        {
            k1 = RenderSystem.reAlpha(CustomColors.getExpBarTextColor(k1), 1.0f);
        }

        String s = String.valueOf(this.mc.thePlayer.experienceLevel);

        FontManager.pf16.drawOutlineCenteredString(s, hotbarX + hotbarWidth * 0.5, hotbarY - y + expBarHeight * 0.5 - FontManager.pf16.getHeight() * 0.5, k1, RenderSystem.hexColor(0, 0, 0));

//        FontManager.pf25bold.drawString(String.valueOf(mc.thePlayer.experience), 100, 100, Color.BLACK.getRGB());
    }

    private void drawHealthBar(double hotbarX, double hotbarY, double hotbarWidth) {
        double healthBarWidth = (hotbarWidth - 8) / 2;
        double healthBarHeight = 10;
        double totalHealth = mc.thePlayer.getMaxHealth() + mc.thePlayer.getAbsorptionAmount();
        double healthWidth = mc.thePlayer.getHealth() / totalHealth * healthBarWidth;
        double abspWidth = mc.thePlayer.getAbsorptionAmount() / totalHealth * healthBarWidth;

        hurtAnimPerc = AnimationSystem.interpolate(hurtAnimPerc, (double) mc.thePlayer.hurtTime / (mc.thePlayer.maxHurtTime + 0.00001), 0.25);
        hurtAnimAlpha = (int) AnimationSystem.interpolate(hurtAnimAlpha, hurtAnimPerc * 120, 0.4);

        if (Double.isNaN(targetHealthWidth)) {
            targetHealthWidth = healthWidth;
        }

        if (Double.isNaN(targetAbspWidth)) {
            targetAbspWidth = abspWidth;
        }

        if (mc.thePlayer.getHealth() != lastHealth) {
            double delta = lastHealth - mc.thePlayer.getHealth();
            if (delta >= 0)
                damageDealt = delta;
            lastHealth = mc.thePlayer.getHealth();
            damageTimer.reset();
        }

        if (damageTimer.isDelayed(1000)) {
            lastHealth = mc.thePlayer.getHealth();
            damageDealt = 0;
            damageTimer.reset();
        }

        targetHealthWidth = AnimationSystem.interpolate(targetHealthWidth, healthWidth, 0.4f);
        if (targetHealthWidth <= healthWidth + 0.1)
            targetHealthWidthLast = AnimationSystem.interpolate(targetHealthWidthLast, targetHealthWidth, 0.2f);
        targetAbspWidth = AnimationSystem.interpolate(targetAbspWidth, abspWidth, 0.4f);

        ShaderUtils.doRectBlurAndBloom(hotbarX, hotbarY - 30, healthBarWidth, healthBarHeight);

        Rect.draw(hotbarX, hotbarY - 30, healthBarWidth, healthBarHeight, new Color(0, 0, 0, 50).getRGB(), Rect.RectType.EXPAND);
        Rect.draw(hotbarX, hotbarY - 30, targetHealthWidthLast, healthBarHeight, new Color(255, 100, 80, hurtAnimAlpha).getRGB(), Rect.RectType.EXPAND);

        Rect.draw(hotbarX, hotbarY - 30, targetHealthWidth, healthBarHeight, RenderSystem.reAlpha(this.getHealthColor(mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()), 0.5f), Rect.RectType.EXPAND);

        targetHealthPercent = AnimationSystem.interpolateApprox(targetHealthPercent, ((mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) * 100), 0.15);

        if (mc.thePlayer.getAbsorptionAmount() > 0) {
            Rect.draw(hotbarX + targetHealthWidth, hotbarY - 30, targetAbspWidth, healthBarHeight, RenderSystem.hexColor(253, 173, 0), Rect.RectType.EXPAND);

            String abspText = String.valueOf((int) (mc.thePlayer.getAbsorptionAmount() / 2));


            double abspX;

            if (targetAbspWidth - (FontManager.pf14.getStringWidth(abspText) + 6) < 0) {
                abspX = hotbarX + targetHealthWidth + targetAbspWidth + 2;
            } else {
                abspX = hotbarX + targetHealthWidth + targetAbspWidth - FontManager.pf16.getStringWidth(abspText) - 2;
            }

            FontManager.pf14.drawStringWithBetterShadow(abspText, abspX, hotbarY - 29.5 + healthBarHeight / 2.0 - FontManager.pf14.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));

        }

        String percentText = (int) targetHealthPercent + "%";

        if (hurtAnimAlpha > 3) {
            double dealtY = hotbarY - 29.5 + healthBarHeight / 2.0 - FontManager.pf16.getHeight() / 2.0;

            if (healthBarWidth - 4 - targetHealthWidthLast < FontManager.pf16.getStringWidth("-" + df.format(damageDealt)))
                dealtY = hotbarY - 30 + healthBarHeight * 1.5 - FontManager.pf16.getHeight() / 2.0;

            FontManager.pf16.drawString("-" + df.format(damageDealt), hotbarX + 2 + targetHealthWidthLast + targetAbspWidth + ((targetHealthWidth + targetAbspWidth - (FontManager.pf16.getStringWidth(percentText) + 4) < 0) ? (FontManager.pf16.getStringWidth(percentText) + 2) : 0), dealtY, new Color(255, 0, 0, hurtAnimAlpha * 2).getRGB());
        }

        double percentX;

        if (targetHealthWidth - (FontManager.pf14.getStringWidth(percentText) + 6) < 0) {
            percentX = hotbarX + targetHealthWidth + 4;
        } else {
            percentX = hotbarX + targetHealthWidth - FontManager.pf16.getStringWidth(percentText);
        }

        FontManager.pf14.drawStringWithBetterShadow(percentText, percentX, hotbarY - 29.5 + healthBarHeight / 2.0 - FontManager.pf14.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));

    }

    private void drawFoodBar(double hotbarX, double hotbarY, double hotbarWidth) {
        double foodBarWidth = (hotbarWidth - 8) / 2;
        double foodBarHeight = 10;
        double totalFood = 20;
        double foodWidth = mc.thePlayer.getFoodStats().getFoodLevel() / totalFood * foodBarWidth;

        if (Double.isNaN(targetFoodWidth)) {
            targetFoodWidth = foodWidth;
        }

        targetFoodWidth = AnimationSystem.interpolate(targetFoodWidth, foodWidth, 0.4f);

        ShaderUtils.doRectBlurAndBloom(hotbarX + foodBarWidth + 8, hotbarY - 30, foodBarWidth, foodBarHeight);

        Rect.draw(hotbarX + foodBarWidth + 8, hotbarY - 30, foodBarWidth, foodBarHeight, new Color(0, 0, 0, 50).getRGB(), Rect.RectType.EXPAND);

        Rect.draw(hotbarX + foodBarWidth + 8, hotbarY - 30, targetFoodWidth, foodBarHeight, new Color(255, 98, 0, 100).getRGB(), Rect.RectType.EXPAND);

        targetFoodPercent = AnimationSystem.interpolateApprox(targetFoodPercent, ((mc.thePlayer.getFoodStats().getFoodLevel() / totalFood) * 100), 0.15);

        String percentText = (int) targetFoodPercent + "%";

        double percentX;

        if (targetFoodWidth - (FontManager.pf14.getStringWidth(percentText) + 6) < 0) {
            percentX = hotbarX + foodBarWidth + 8 + targetFoodWidth + 4;
        } else {
            percentX = hotbarX + foodBarWidth + 8 + targetFoodWidth - 2 - FontManager.pf14.getStringWidth(percentText);
        }

        FontManager.pf14.drawStringWithBetterShadow(percentText, percentX, hotbarY - 29.5 + foodBarHeight / 2.0 - FontManager.pf14.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));
    }

    private void drawArmorBar(double hotbarX, double hotbarY, double hotbarWidth) {
        double armorBarWidth = (hotbarWidth - 8) / 2;
        double armorBarHeight = 10;
        double totalArmor = 20;
        double armorWidth = mc.thePlayer.getTotalArmorValue() / totalArmor * armorBarWidth;

        if (Double.isNaN(targetArmorWidth)) {
            targetArmorWidth = armorWidth;
        }

        targetArmorWidth = AnimationSystem.interpolate(targetArmorWidth, armorWidth, 0.4f);

        ShaderUtils.doRectBlurAndBloom(hotbarX, hotbarY - 34 - armorBarHeight, armorBarWidth, armorBarHeight);

        Rect.draw(hotbarX, hotbarY - 34 - armorBarHeight, armorBarWidth, armorBarHeight, new Color(0, 0, 0, 50).getRGB(), Rect.RectType.EXPAND);

        Rect.draw(hotbarX, hotbarY - 34 - armorBarHeight, targetArmorWidth, armorBarHeight, new Color(0, 159, 255, 150).getRGB(), Rect.RectType.EXPAND);

        targetArmorPercent = AnimationSystem.interpolateApprox(targetArmorPercent, ((mc.thePlayer.getTotalArmorValue() / totalArmor) * 100), 0.15);

        String percentText = (int) targetArmorPercent + "%";

        double percentX;

        if (targetArmorWidth - (FontManager.pf14.getStringWidth(percentText) + 4) < 0) {
            percentX = hotbarX + targetArmorWidth + 2;
        } else {
            percentX = hotbarX + targetArmorWidth - FontManager.pf16.getStringWidth(percentText);
        }

        FontManager.pf14.drawStringWithBetterShadow(percentText, percentX, hotbarY - 33.5 - armorBarHeight + armorBarHeight / 2.0 - FontManager.pf14.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));
    }

    private int getHealthColor(double percent) {
        if (percent <= 0.6 && percent > 0.3) {
            return new Color(253, 173, 0).getRGB();
        } else if (percent <= 0.3) {
            return Color.RED.getRGB();
        } else {
            return new Color(57, 199, 56).getRGB();
        }
    }

    private double getSwordDamage(final ItemStack itemStack) {
        double damage = 0.0;
        final Optional<AttributeModifier> attributeModifier = itemStack.getAttributeModifiers().values().stream().findFirst();
        if (attributeModifier.isPresent()) {
            damage = attributeModifier.get().getAmount();
        }
        return damage + EnchantmentHelper.getModifierForCreature(itemStack, EnumCreatureAttribute.UNDEFINED);
    }

    private void renderItem(ItemStack itemStack, int x, int y, double scale) {

        GlStateManager.pushMatrix();

        RenderHelper.enableGUIStandardItemLighting();
        this.renderItemIntoGUI(itemStack, x, y, scale);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }

    public void renderItemIntoGUI(ItemStack stack, int x, int y, double scale) {
        mc.getRenderItem().renderItemGui = true;
        IBakedModel ibakedmodel = mc.getRenderItem().itemModelMesher.getItemModel(stack);
        GlStateManager.pushMatrix();
        mc.getRenderItem().textureManager.bindTexture(TextureMap.locationBlocksTexture);
        mc.getRenderItem().textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getRenderItem().setupGuiTransform(x, y, ibakedmodel.isGui3d());

        ibakedmodel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);


        this.renderItem(stack, ibakedmodel, scale);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();

        if (mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture) == null) {
            mc.getTextureManager().loadTickableTexture(TextureMap.locationBlocksTexture, mc.getTextureMapBlocks());
        }

        mc.getRenderItem().textureManager.bindTexture(TextureMap.locationBlocksTexture);
        mc.getRenderItem().textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        mc.getRenderItem().renderItemGui = false;
    }

    public void renderItem(ItemStack stack, IBakedModel model, double scale) {
        if (stack != null) {
            GlStateManager.pushMatrix();
            current = RenderSystem.createFrameBuffer(current);
            GlStateManager.scale(0.5F + scale, 0.5F + scale, 0.5F + scale);

            if (model.isBuiltInRenderer()) {
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                TileEntityItemStackRenderer.instance.renderByItem(stack);
            } else {
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);

                if (Config.isCustomItems()) {
                    model = CustomItems.getCustomItemModel(stack, model, mc.getRenderItem().modelLocation, false);
                }

                mc.getRenderItem().renderModelHasEmissive = false;
                mc.thePlayer.getCurrentEquippedItem();//                    current.framebufferClear();
//                    current.bindFramebuffer(true);
                mc.getRenderItem().renderModel(model, stack);
                mc.thePlayer.getCurrentEquippedItem();//                    current.unbindFramebuffer();

                if (mc.getRenderItem().renderModelHasEmissive) {
                    float f = OpenGlHelper.lastBrightnessX;
                    float f1 = OpenGlHelper.lastBrightnessY;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, f1);
                    mc.getRenderItem().renderModelEmissive = true;
                    mc.getRenderItem().renderModel(model, stack);
                    mc.getRenderItem().renderModelEmissive = false;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
                }

                if (stack.hasEffect() && (!Config.isCustomItems() || !CustomItems.renderCustomEffect(mc.getRenderItem(), stack, model))) {
                    mc.getRenderItem().renderEffect(model);
                }
            }

            GlStateManager.popMatrix();

            mc.thePlayer.getCurrentEquippedItem();//                current.framebufferRender(mc.displayWidth, mc.displayHeight);
//                GlowShader.renderGlow(current.framebufferTexture, 5, 0.6f, Color.YELLOW.getRGB(), 1);
        }
    }
}
