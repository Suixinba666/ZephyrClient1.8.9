package tech.imxianyu.module.impl.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Formatting;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Mouse;
import tech.imxianyu.Zephyr;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.input.KeyPressedEvent;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.management.WidgetsManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.render.hud.TabGuiRenderer;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.notification.NotificationManager;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.titlebar.TitleBar;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.widget.impl.Music;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.opengl.GL11;

/**
 * @author ImXianyu
 * @since 4/15/2023 7:45 PM
 */
public class Hud extends Module {
    public NumberSetting<Float> fireY = new NumberSetting<>("First Person Fire Y", -0.3F, -1.0F, 0.3F, 0.01F);

    public BooleanSetting hurtCamDirection = new BooleanSetting("Hurt Cam Direction", false);
    public BooleanSetting waterMark = new BooleanSetting("Water Mark", true);
    public BooleanSetting tabGui = new BooleanSetting("Tab Gui", true);
    public BooleanSetting notifications = new BooleanSetting("Notifications", true);
    public BooleanSetting renderInfo = new BooleanSetting("Render Info", false);
    TabGuiRenderer tabGuiRenderer;
    DecimalFormat df = new DecimalFormat("##.##");

    @Handler
    public void onRender2D(Render2DEvent event) {
//        FontManager.shs25.drawString("\247kk", 50, 50, -1);
        if (this.renderInfo.getValue())
            this.renderInfo();

        if (this.waterMark.getValue())
            this.renderWaterMark();

        if (this.tabGui.getValue()) {
            if (tabGuiRenderer == null) {
                tabGuiRenderer = new TabGuiRenderer();
            }
            this.tabGuiRenderer.draw(8, 10 + TitleBar.getHeight() + (this.waterMark.getValue() ? 22 : 0));
        }

        if (this.notifications.getValue())
            NotificationManager.doRender(4, 10 + TitleBar.getHeight() + (this.waterMark.getValue() ? 22 : 0) + (this.tabGui.getValue() ? 18 * Category.values().length + 8 : 0));

//        FontManager.gsans40.drawString("Mouse.grabbed() = " + Mouse.isGrabbed(), 100, 100, -1);
    };

    @Handler
    public void onKeyPressed(KeyPressedEvent event) {
        if (this.tabGui.getValue())
            this.tabGuiRenderer.keyTyped(event.getKeyCode());
    };


    private void renderWaterMark() {
//        Image.draw(new ResourceLocation("Zephyr/textures/logo_128x.png"), 0, 0, 64, 64, Image.Type.Normal);

        String msg = Formatting.BOLD + "ZEPHYR" + Formatting.RESET + " | " + mc.getSession().getUsername() + " | ";

        if (mc.getCurrentServerData() != null) {
            msg += mc.getCurrentServerData().serverIP + " | ";
        }

        msg += LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        ZFontRenderer fontRenderer = FontManager.baloo18;

        double width = fontRenderer.getStringWidth(msg) + 8;
        double height = fontRenderer.getHeight() + 4;
        double posX = 4, posY = 6 + TitleBar.getHeight();


        if (ZephyrSettings.reduceShaders.getValue()) {
            RoundedRect.drawRound(posX, posY, width, height, 2, new Color(ColorUtils.getColor(ColorUtils.ColorType.Base)));

        } else {
            ShaderUtils.doRoundedBlurAndBloom(posX, posY, width, height, 2);
            RoundedRect.drawRound(posX, posY, width, height, 2, new Color(0, 0, 0, 50));
        }

        fontRenderer.drawString(msg, posX + 4, posY + height * 0.5 - (fontRenderer.getHeight()) * 0.5, -1);

    }

    private void renderInfo() {

        ZFontRenderer fontRenderer = FontManager.pf18;
        double offsetY = RenderSystem.getHeight() - fontRenderer.getHeight();

        List<String> infoRight = this.getInfoRight();

        infoRight.addAll(this.getInfoLeft());

        for (String s : infoRight) {
            fontRenderer.drawOutlineString(s, RenderSystem.getWidth() - fontRenderer.getStringWidth(s) - 2, offsetY, -1, RenderSystem.hexColor(0, 0, 0));
            offsetY -= fontRenderer.getHeight();
        }

    }

    private List<String> getInfoLeft() {

        double xDiff = (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * 2;
        double zDiff = (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * 2;
        BigDecimal bg = BigDecimal.valueOf(MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff) * 10d);

        return new ArrayList<>(
                Arrays.asList(
                        String.format("X: %s Y: %s Z: %s", df.format(mc.thePlayer.posX), df.format(mc.thePlayer.posY), df.format(mc.thePlayer.posZ)),
                        String.format("Speed: %s", df.format(bg.doubleValue() * mc.timer.timerSpeed)),
                        String.format("FPS: %s", df.format(Minecraft.getDebugFPS()))
                )
        );
    }

    private List<String> getInfoRight() {
        return new ArrayList<>(
                Arrays.asList(
                        String.format("%sBuild %s%s %s%s", Formatting.GRAY, Formatting.GOLD, Zephyr.getInstance().getVersion().getType().name(), Formatting.GREEN, Zephyr.getInstance().getVersion().getBuildDate())
                )
        );
    }

    public Hud() {
        super("Hud", Category.RENDER);

        super.setEnabled(true);
    }

}
