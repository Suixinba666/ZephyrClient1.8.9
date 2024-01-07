package tech.imxianyu.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Formatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.widget.Widget;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ImXianyu
 * @since 6/20/2023 10:17 AM
 */
public class Potion extends Widget {
    private final ZFontRenderer fontRenderer = FontManager.pf18;
    public BooleanSetting icon = new BooleanSetting("Icon", true);
    public BooleanSetting text = new BooleanSetting("Text", true);
    public BooleanSetting time = new BooleanSetting("Time", true);
    public BooleanSetting rect = new BooleanSetting("Rect", true);
    public Map<PotionEffect, PotionEffectEntity> entityMap = new HashMap<>();
    double renderWidth, renderHeight;

    public Potion() {
        super("Potion");
    }

    @Override
    public void onRender(Render2DEvent event, boolean editing) {
        double width = 180, height = 35, spacing = 6;
        this.renderWidth = AnimationSystem.interpolate(this.renderWidth, width, 0.2f);
        this.renderHeight = AnimationSystem.interpolate(this.renderHeight, (height + spacing) * /*(isEditing ? 1 : */mc.thePlayer.getActivePotionEffects().size()/*)*/ - spacing, 0.2f);

        this.setWidth(this.renderWidth);
        this.setHeight(this.renderHeight);

        double offsetX = this.getX(), offsetY = this.getY();


        for (PotionEffect activePotionEffect : mc.thePlayer.getActivePotionEffects()) {
            PotionEffectEntity entity = this.getEntity(activePotionEffect);

            entity.draw(offsetX, offsetY);

            offsetY += height + spacing;
        }
    }

    private PotionEffectEntity getEntity(PotionEffect effect) {
        if (!this.entityMap.containsKey(effect)) {
            this.entityMap.put(effect, new PotionEffectEntity(effect));
        }

        return this.entityMap.get(effect);
    }

    private class PotionEffectEntity {
        public final PotionEffect effect;
        double durationWidth;

        public PotionEffectEntity(PotionEffect effect) {
            this.effect = effect;
        }

        public void draw(double posX, double posY) {
            double width = 180, height = 35, spacing = 6;
//            TexturedShadow.drawShadow(posX, posY, width, height, 1.0f, 8);

            ShaderUtils.doRectBlurAndBloom(posX, posY, durationWidth, height);

            net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[effect.getPotionID()];
            String name = I18n.format(potion.getName());
            String level = " " + (effect.getAmplifier() + 1);
            int potionEffectDuration = effect.getDuration();
            durationWidth = AnimationSystem.interpolate(durationWidth, effect.totalDuration == 0 ? 0 : (width * ((double) potionEffectDuration / (double) effect.totalDuration)), 0.2f);
            String timeString = net.minecraft.potion.Potion.getDurationString(effect);
            if (level.trim().equals("1")) {
                level = "";
            }

            if (icon.getValue() && potion.hasStatusIcon()) {
                GlStateManager.pushMatrix();
                GL11.glDisable(2929);
                GL11.glEnable(3042);
                GL11.glDepthMask(false);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                int statusIconIndex = potion.getStatusIconIndex();
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                GlStateManager.translate(posX + 2, posY + 2, 0);
                GlStateManager.scale(1.8, 1.8, 0);
                mc.ingameGUI.drawTexturedModalRect(0, 0, statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18);
                GL11.glDepthMask(true);
                GL11.glDisable(3042);
                GL11.glEnable(2929);
                GlStateManager.popMatrix();
            }

            String color = "";
            if (potionEffectDuration > 0) {
                color = Formatting.RED.toString();
            }
            if (potionEffectDuration > 300) {
                color = Formatting.GOLD.toString();
            }
            if (potionEffectDuration > 600) {
                color = Formatting.DARK_GRAY.toString();
            }

            if (text.getValue()) {
                fontRenderer.drawString(name + level, posX + 38, posY + 5, RenderSystem.reAlpha(potion.getLiquidColor(), 1.0f));
            }

            if (time.getValue()) {
                fontRenderer.drawString(color + timeString, posX + 38, posY + 8 + fontRenderer.getHeight(), -1);
            }
        }
    }
}
