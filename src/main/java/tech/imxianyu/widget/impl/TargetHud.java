package tech.imxianyu.widget.impl;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjglx.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.glu.GLU;
import tech.imxianyu.Zephyr;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.events.world.WorldChangedEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.*;
import tech.imxianyu.rendering.animation.Animation;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.animation.animations.MultiEndpointAnimation;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.FontUtils;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.utils.timing.Timer;
import tech.imxianyu.widget.Widget;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ImXianyu
 * @since 6/27/2023 2:49 PM
 */
public class TargetHud extends Widget {

    public ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Simple);
    public Map<EntityLivingBase, TargetBean> targets = new HashMap<>();
    @Handler
    public void onWorldChanged(WorldChangedEvent event) {
        targets.clear();
    };
    @Handler
    public void onRender3D(Render3DEvent event) {
        if (mode.getValue() != Mode.FollowTarget)
            return;

        for (TargetBean value : this.targets.values()) {
            value.updateFollowPositions(event);
        }
    };
    @Handler
    public void onAttack(SendPacketEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity && ((C02PacketUseEntity) event.getPacket()).getAction() == C02PacketUseEntity.Action.ATTACK) {

            Entity entityFromWorld = ((C02PacketUseEntity) event.getPacket()).getEntityFromWorld(mc.theWorld);

            if (entityFromWorld instanceof EntityLivingBase) {
                if (entityFromWorld.getDisplayName().getUnformattedText().equals("bot"))
                    return;
                if (!this.targets.containsKey((EntityLivingBase) entityFromWorld)) {
                    this.targets.put((EntityLivingBase) entityFromWorld, new TargetBean((EntityLivingBase) entityFromWorld));
                } else {
                    this.targets.get((EntityLivingBase) entityFromWorld).target = (EntityLivingBase) entityFromWorld;
                }
                this.targets.get((EntityLivingBase) entityFromWorld).mainTimer.reset();
                this.targets.get((EntityLivingBase) entityFromWorld).subTimer.reset();
            }
        }
    };
    long delay = 1000;
    @Handler
    public void onTick(TickEvent event) {
        delay = 1000;
        this.targets.forEach((t, targetHud) -> {
            if (targetHud.mainTimer.isDelayed(delay) && !targetHud.isCloseRequested && targetHud.percent >= 0.9) {
                targetHud.isCloseRequested = true;
            }
        });
    };
    DecimalFormat df = new DecimalFormat("#.##");
    int ordinary = 0;

    public TargetHud() {
        super("Target Hud");
    }

    @Override
    public void onDisable() {
        targets.clear();
    }

    @Override
    public void onRender(Render2DEvent event, boolean editing) {
        if (editing) {
            if (!this.targets.containsKey(mc.thePlayer))
                this.targets.put(mc.thePlayer, new TargetBean(mc.thePlayer));
            this.targets.get(mc.thePlayer).target = mc.thePlayer;

            this.targets.get(mc.thePlayer).mainTimer.reset();
            this.targets.get(mc.thePlayer).subTimer.reset();
            this.targets.get(mc.thePlayer).isCloseRequested = false;
            ordinary = 0;
            this.targets.get(mc.thePlayer).onRender(true);
        }

        if (targets.isEmpty()) {
            return;
        }


        if (editing)
            ordinary = 1;
        else
            ordinary = 0;

        for (TargetBean value : this.targets.values()) {
            if ((value.target == mc.thePlayer || value.target instanceof EntityPlayerSP)) {
                if (!value.mainTimer.isDelayed(delay) && editing) {
                    value.mainTimer.lastNs = System.currentTimeMillis() + delay * 2;
                    value.percent = 0;
                }
                continue;
            }
            value.onRender(editing);
            ++ordinary;
        }

        this.setWidth(140);
        this.setHeight(50);
    }

    public enum Mode {
        Simple,
        FollowTarget
    }

    class TargetBean {
        final ResourceLocation skin;
        private final Timer mainTimer = new Timer(), subTimer = new Timer();
        private final Animation animation = new MultiEndpointAnimation().withStartValue(0).withEndPoints(0.0, 1.1, 1);
        public float percent = 0;
        public double damageDealt = 0;
        public Timer damageTimer = new Timer();
        public double lastHealth;
        double posX, posY;
        EntityLivingBase target;
        double hurtAnimPerc = 1.0;
        int hurtAnimAlpha = 0;
        double hurtDownAnim = 0;
        int nextRowLimit = 3;
        private boolean isCloseRequested = false;
        private double targetHealthWidth = 0, targetHealthPercent = 0;
        private double targetAbspWidth = 0;
        private double targetHealthWidthLast = 0;
        private double healthBarHeight = 10;

        public TargetBean(EntityLivingBase target) {
            Render<Entity> entityRender = mc.getRenderManager().getEntityRenderObject(target);
            this.skin = entityRender.getEntityTexture(target);
            this.target = target;
            this.animation.reset();
            this.lastHealth = this.target.getHealth();
        }

        public void updateFollowPositions(Render3DEvent event) {
            float pTicks = mc.timer.renderPartialTicks;

            Vector4d position = this.convertTo2D(target, event);

            if (position == null) return;

//            this.position.x = position.z;
//            this.position.y = position.w - (position.w - position.y) / 2/* - this.positionValue.scale.y / 2f*/;

            this.posX = AnimationSystem.interpolate(this.posX, position.z, 0.2f);
            this.posY = AnimationSystem.interpolate(this.posY, position.w - (position.w - position.y) / 2, 0.2f);

//            if (desX < -125 || desX > RenderSystem.getWidth() || desY < -66 || desY > RenderSystem.getHeight()) {
//                this.posX = this.posY = -200;
//            } else if (this.posX == -200 || this.posY == -200) {
//                this.posX = desX;
//                this.posY = desY;
//            }
        }

        public void onRender(boolean isEditing) {
            GlStateManager.pushMatrix();
            double posX = getX() + this.posX;
            double posY = getY() + this.posY;

            if (mode.getValue() == Mode.FollowTarget) {
                posX = this.posX;
                posY = this.posY;
            }

            if (mode.getValue() == Mode.Simple) {
                this.posX = AnimationSystem.interpolate(this.posX, (ordinary / nextRowLimit) * 160.0, 0.2f);

                this.posY = AnimationSystem.interpolate(this.posY, ordinary % nextRowLimit * 60.0, 0.2f);
            }

            if (this.mainTimer.isDelayed(delay) && !isCloseRequested && percent <= 0.4) {
                GlStateManager.popMatrix();
                ordinary -= 1;
                return;
            }

            double width = 140, height = 50;

            //Animation
            if (!isEditing) {
                if (this.isCloseRequested/* || (!mc.thePlayer.canEntityBeSeen(target) || !RotationUtils.isVisibleFOV(target, 180))*/) {
                    percent = (float) animation.interpolate(true, 0.2f);
                } else {
                    percent = (float) animation.interpolate(false, 0.2f);
                }
                if (percent <= 0.4 && (this.isCloseRequested/* || (!mc.thePlayer.canEntityBeSeen(target) || !RotationUtils.isVisibleFOV(target, 180))*/)) {
                    this.isCloseRequested = false;
                    this.animation.reset();
                    ordinary -= 1;
                }
                if (percent != 0 || percent < 0.95) {
                    GlStateManager.translate(posX + 62.5, posY + 33, 0);
                    GlStateManager.scale(percent, percent, 0);
                    GlStateManager.translate(-(posX + 62.5), -(posY + 33), 0);
                }
            }

            hurtDownAnim = AnimationSystem.interpolate(hurtDownAnim, hurtAnimPerc * 8, 0.2f);
            GlStateManager.translate(0, hurtDownAnim, 0);

            if (target == null) {
                GlStateManager.popMatrix();
                ordinary -= 1;
                return;
            }

            ShaderUtils.doRectBlurAndBloom(posX, posY, width, height);

            if (target.getHealth() != lastHealth) {
                double delta = lastHealth - target.getHealth();
                if (delta >= 0)
                    damageDealt = delta;
                lastHealth = target.getHealth();
                damageTimer.reset();
            }

            if (damageTimer.isDelayed(1000)) {
                lastHealth = target.getHealth();
                damageDealt = 0;
                damageTimer.reset();
            }


            GlStateManager.pushMatrix();
            hurtAnimPerc = AnimationSystem.interpolate(hurtAnimPerc, (double) target.hurtTime / (target.maxHurtTime + 0.00001), 0.4);
            hurtAnimAlpha = (int) AnimationSystem.interpolate(hurtAnimAlpha, hurtAnimPerc * 120, 0.6);
            GlStateManager.translate(posX + 20, posY + 20, 0);
            GlStateManager.scale(1 - hurtAnimPerc * 0.2, 1 - hurtAnimPerc * 0.2, 0);
            TexturedShadow.drawShadow(-16, -16, 32, 32, 1.0f, 6);

            healthBarHeight = AnimationSystem.interpolate(healthBarHeight, subTimer.isDelayed(250) ? 5 : 10, 0.2f);

            GlStateManager.color(1, 1, 1, 1);

            mc.getTextureManager().bindTexture(skin);
            Gui.drawScaledCustomSizeModalRect(-16, -16, 8.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
            if (target instanceof EntityPlayer) {
                if (((EntityPlayer) target).isWearing(EnumPlayerModelParts.HAT)) {
                    Gui.drawScaledCustomSizeModalRect(-16, -16, 40.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
                }
            }

            GlStateManager.bindTexture(0);

            GlStateManager.popMatrix();
            if (hurtAnimAlpha != 0) {
//                RoundedRect.drawRound(posX, posY, width, height, 8, new Color(255, 0, 0, (int) (hurtAnimAlpha * 0.8)));
                Rect.draw(posX, posY, width, height, RenderSystem.hexColor(255, 0, 0, (int) (hurtAnimAlpha * 0.8)), Rect.RectType.EXPAND);
            }

            if (FontManager.pf20.getStringWidth(target.getName()) <= width - 44) {
                FontManager.pf20.drawStringWithBetterShadow(target.getName(), posX + 40, posY + 4, ColorUtils.getColor(ColorUtils.ColorType.Text));
            } else {
                FontUtils.getFontRendererByWidth(target.getName(), width - 42).drawStringWithBetterShadow(target.getName(), posX + 40, posY + 4, ColorUtils.getColor(ColorUtils.ColorType.Text));
            }

            List<String> info = Arrays.asList(
                    "Health: " + df.format(target.getHealth()),
                    "HurtTime: " + target.hurtResistantTime,
                    mc.thePlayer.getHealth() > target.getHealth() ? "Winning" : (mc.thePlayer.getHealth() == target.getHealth() ? "Draw" : "Losing")
            );

            double offsetX = posX + 40;
            double offsetY = posY + 2 + FontManager.pf25.getHeight();

            for (String s : info) {
                FontManager.pf14.drawString(s, offsetX, offsetY, RenderSystem.reAlpha(ColorUtils.getColor(ColorUtils.ColorType.Text), 0.8f));
                offsetY += FontManager.pf14.getHeight();
            }


            GL11.glColor4f(1, 1, 1, 1);
            double healthBarWidth = width;
            double totalHealth = target.getMaxHealth() + target.getAbsorptionAmount();
            double healthWidth = target.getHealth() / totalHealth * healthBarWidth;
            double abspWidth = target.getAbsorptionAmount() / totalHealth * healthBarWidth;
            double healthBarX = posX;
            double healthBarY = posY + height - healthBarHeight;

            if (Double.isNaN(targetHealthWidth)) {
                targetHealthWidth = healthWidth;
            }

            if (Double.isNaN(targetAbspWidth)) {
                targetAbspWidth = abspWidth;
            }

            targetHealthWidth = AnimationSystem.interpolate(targetHealthWidth, healthWidth, 0.4f);
            if (targetHealthWidth <= healthWidth + 0.1)
                targetHealthWidthLast = AnimationSystem.interpolate(targetHealthWidthLast, targetHealthWidth, 0.2f);
            targetAbspWidth = AnimationSystem.interpolate(targetAbspWidth, abspWidth, 0.4f);
            double targetHealthShrink = 2;
//            RenderUtil.drawShadow(posX + 5 + targetHealthShrink, posY + 48 + targetHealthShrink, Math.max(0, targetHealthWidth + targetAbspWidth - targetHealthShrink * 2), 10 - targetHealthShrink * 2, 1.0f, 6 + targetHealthShrink);

            new Rect(healthBarX, healthBarY, targetHealthWidthLast + targetAbspWidth, healthBarHeight, new Color(255, 0, 0, hurtAnimAlpha * 2).getRGB(), Rect.RectType.EXPAND).draw();


            new Rect(healthBarX, healthBarY, targetHealthWidth, healthBarHeight, RenderSystem.reAlpha(this.getHealthColor(target.getHealth() / target.getMaxHealth()), 0.5f), Rect.RectType.EXPAND).draw();

            if (target.getAbsorptionAmount() > 0) {
                new Rect(healthBarX + targetHealthWidth, healthBarY, targetAbspWidth, healthBarHeight, RenderSystem.hexColor(253, 173, 0), Rect.RectType.EXPAND).draw();

                String abspText = String.valueOf((int) (target.getAbsorptionAmount() / 2));

                double abspX;

                if (targetAbspWidth - (FontManager.pf14.getStringWidth(abspText) + 6) < 0) {
                    abspX = healthBarX + targetHealthWidth + targetAbspWidth + 2;
                } else {
                    abspX = healthBarX + targetHealthWidth + targetAbspWidth - FontManager.pf16.getStringWidth(abspText) - 2;
                }

                FontManager.pf16.drawStringWithBetterShadow(abspText, abspX, healthBarY + healthBarHeight / 2.0 - FontManager.pf16.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));

            }

            targetHealthPercent = AnimationSystem.interpolateApprox(targetHealthPercent, (((target.getHealth() + target.getAbsorptionAmount()) / target.getMaxHealth()) * 100), 0.15);

            if (healthBarHeight > 8) {
                String percentText = (int) targetHealthPercent + "%";

                if (hurtAnimAlpha > 3) {
                    double dealtY = healthBarY + healthBarHeight / 2.0 - FontManager.pf16.getHeight() / 2.0;

                    if (healthBarWidth - 4 - targetHealthWidthLast < FontManager.pf16.getStringWidth("-" + df.format(damageDealt)))
                        dealtY = healthBarY + healthBarHeight * 1.5 - FontManager.pf16.getHeight() / 2.0;

                    FontManager.pf16.drawString("-" + df.format(damageDealt), healthBarX + 2 + targetHealthWidthLast + targetAbspWidth + ((targetHealthWidth + targetAbspWidth - (FontManager.pf16.getStringWidth(percentText) + 4) < 0) ? (FontManager.pf16.getStringWidth(percentText) + 2) : 0), dealtY, new Color(255, 0, 0, hurtAnimAlpha * 2).getRGB());
                }

                double percentX;

                if (targetHealthWidth - (FontManager.pf14.getStringWidth(percentText) + 6) < 0) {
                    percentX = healthBarX + targetHealthWidth + 4;
                } else {
                    percentX = healthBarX + targetHealthWidth - FontManager.pf16.getStringWidth(percentText);
                }

                FontManager.pf16.drawStringWithBetterShadow(percentText, percentX, healthBarY + healthBarHeight / 2.0 - FontManager.pf16.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));


            }


            GlStateManager.popMatrix();
//            GlStateManager.popAttrib();
        }

        public Vector4d convertTo2D(EntityLivingBase entity, Render3DEvent event) {
            final double renderX = mc.getRenderManager().renderPosX;
            final double renderY = mc.getRenderManager().renderPosY;
            final double renderZ = mc.getRenderManager().renderPosZ;
            ScaledResolution res = new ScaledResolution(mc);
            final double factor = res.getScaleFactor();
            final float partialTicks = event.partialTicks;

            final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - renderX;
            final double y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks) - renderY;
            final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - renderZ;
            final double width = (entity.width + 0.2) / 2;
            final double height = entity.height + (entity.isSneaking() ? -0.3D : 0.2D) + 0.05;
            final AxisAlignedBB aabb = new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width);
            final List<Vector3d> vectors = Arrays.asList(new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.minX, aabb.maxY, aabb.minZ), new Vector3d(aabb.maxX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vector3d(aabb.minX, aabb.minY, aabb.maxZ), new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ));

            Vector4d position = null;
            for (Vector3d vector : vectors) {
                vector = project(factor, vector.getX(), vector.getY(), vector.getZ());

                if (vector != null && vector.getZ() >= 0.0D && vector.getZ() < 1.0D) {
                    if (position == null) {
                        position = new Vector4d(vector.getX(), vector.getY(), vector.getZ(), 0.0D);
                    }

                    position = new Vector4d(Math.min(vector.getX(), position.x), Math.min(vector.getY(), position.y), Math.max(vector.getX(), position.z), Math.max(vector.getY(), position.w));
                }
            }

            return position;
        }

        private Vector3d project(final double factor, final double x, final double y, final double z) {
            if (GLU.gluProject((float) x, (float) y, (float) z, ActiveRenderInfo.MODELVIEW, ActiveRenderInfo.PROJECTION, ActiveRenderInfo.VIEWPORT, ActiveRenderInfo.OBJECTCOORDS)) {
                return new Vector3d((ActiveRenderInfo.OBJECTCOORDS.get(0) / factor), ((Display.getHeight() - ActiveRenderInfo.OBJECTCOORDS.get(1)) / factor), ActiveRenderInfo.OBJECTCOORDS.get(2));
            }

            return null;
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
    }
}
