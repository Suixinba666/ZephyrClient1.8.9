package tech.imxianyu.gui.multiplayer;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.lwjglx.input.Mouse;
import tech.imxianyu.gui.multiplayer.dialog.dialogs.ServerInfoDialog;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.utils.other.StringUtils;
import tech.imxianyu.utils.timing.Timer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

public class ServerBean {

    @Getter
    private final ServerData server;

    public float shadowAlpha = 0.4f, signalHoveredAlpha = 0;
    public double yAnimation = 0;
    @Getter
    private int radius = 8;
    private boolean mouseDown = true;
    private String base64EncodedIconData;
    private final ResourceLocation serverIcon, UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");
    private DynamicTexture serverTexture;
    int signalStrength = -1;

    boolean selected = false;

    RoundedRectWithTriangle roundedRectWithTriangle = new RoundedRectWithTriangle();

    @Getter
    @Setter
    private String status;

    Timer serverMotdScrollTimer = new Timer();

    Minecraft mc = Minecraft.getMinecraft();

    public ServerBean(ServerData server) {
        this.server = server;
        this.serverIcon = new ResourceLocation("servers/" + server.serverIP + "/icon");
        this.serverTexture = (DynamicTexture)this.mc.getTextureManager().getTexture(this.serverIcon);
    }

    public void draw(double x, double y, double width, double height, double mouseX, double mouseY, ZephyrMultiPlayerUI inst) {
        
        double offsetY = y - this.yAnimation;

        inst.behindBloomBuffer.bindFramebuffer(true);
        RoundedRect.drawRound(x, offsetY, width, height, this.getRadius(), new Color(255, 255, 255, (int) (this.shadowAlpha * 255)));
        inst.behindBloomBuffer.unbindFramebuffer();

        mc.getFramebuffer().bindFramebuffer(true);

        RoundedRect.drawRound(x, offsetY, width, height, this.getRadius(), new Color(ColorUtils.getColor(ColorUtils.ColorType.Base)));

        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, x, offsetY, width, height) && inst.dialog == null;

        this.shadowAlpha = AnimationSystem.interpolate(this.shadowAlpha, hovered ? 1.0f : 0.4f, 0.2f);
        this.yAnimation = AnimationSystem.interpolate(this.yAnimation, hovered ? 1 : 0, 0.25);

        boolean signalHovered = RenderSystem.isHovered(mouseX, mouseY, x + width - 22, offsetY + 13, 18, 14) && inst.dialog == null;

        this.signalHoveredAlpha = AnimationSystem.interpolate(this.signalHoveredAlpha, signalHovered ? 1.0f : 0.0f, 0.2f);

        this.checkServer(inst);

        inst.stencilContentBuffer.bindFramebuffer(true);

        if (this.serverTexture != null)
        {
            Image.draw(this.serverIcon, x + 8, offsetY + 15, 34,34, Image.Type.Normal);
        }
        else
        {
            Image.draw(UNKNOWN_SERVER, x + 8, offsetY + 15, 34,34, Image.Type.Normal);
        }

        inst.stencilContentBuffer.unbindFramebuffer();

        inst.stencilCoverBuffer.bindFramebuffer(true);
        RoundedRect.drawRound(x + 9, offsetY + 16, 32,32, 15.5, Color.WHITE);
        inst.stencilCoverBuffer.unbindFramebuffer();

        mc.getFramebuffer().bindFramebuffer(true);

        FontManager.segoe20.drawString(this.server.serverName, x + 50, offsetY + 18, RenderSystem.hexColor(50, 50, 50));
        FontManager.segoe16.drawString(StringUtils.removeFormattingCodes(this.server.populationInfo).isEmpty() ? "N/A" : StringUtils.removeFormattingCodes(this.server.populationInfo), x + 50, offsetY + 30, RenderSystem.hexColor(132, 132, 132));

        List<String> motd = FontManager.pf16.wrapWords(this.server.serverMOTD.trim(), width - 70);
        Iterator<String> it = motd.iterator();

        while (it.hasNext()) {
            String next = it.next().trim();
            if (next.isEmpty() || next.equals("\n"))
                it.remove();
        }

        FontManager.pf16.drawString(String.join("\n", motd), x + 50, offsetY + 43, RenderSystem.hexColor(130, 130, 130));

        double strength = 0;

        if (signalStrength == 0) {
            strength = 30;
        }

        if (signalStrength == 1) {
            strength = 27;
        }

        if (signalStrength == 2) {
            strength = 25;
        }

        if (signalStrength == 3) {
            strength = 22;
        }

        if (signalStrength == 4) {
            strength = 20;
        }

        Image.draw(new ResourceLocation("Zephyr/textures/multiplayer/SignalStrengthBackground.png"), x + width - 36, offsetY, 73, 40, Image.Type.Normal);
        Image.draw(new ResourceLocation("Zephyr/textures/multiplayer/SignalStrength.png"), x + width - 36, offsetY, 73, 40, strength, 40, Image.Type.Normal);

        int statusWidth = FontManager.segoe14.getStringWidth(this.status);

        inst.topBloomBuffer.bindFramebuffer(true);
//        GL11.glColor4f(1, 1, 1, this.signalHoveredAlpha);
//        Image.draw(new ResourceLocation("Zephyr/textures/multiplayer/triangle.png"), x + width - 16, offsetY + 20, 8, 4.5, Image.Type.NoColor);
//        RoundedRect.drawRound(x + width - 14 - statusWidth, offsetY + 20 + 4.5, statusWidth + 8, FontManager.segoe16.getHeight() + 6, 2, new Color(255, 255, 255, Math.min((int) (this.signalHoveredAlpha * 255 + 0.5), 255)));
        roundedRectWithTriangle.draw(x + width - 14 - statusWidth, offsetY + 28, statusWidth + 8, FontManager.segoe16.getHeight() + 6, 8, new Color(255, 255, 255, Math.min((int) (this.signalHoveredAlpha * 255 + 0.5), 255)).getRGB());
        inst.topBloomBuffer.unbindFramebuffer();

        mc.getFramebuffer().bindFramebuffer(true);

//        Image.draw(new ResourceLocation("Zephyr/textures/multiplayer/triangle.png"), x + width - 16, offsetY + 20, 8, 4.5, Image.Type.NoColor);

//        RoundedRect.drawRound(x + width - 14 - statusWidth, offsetY + 20 + 4.5, statusWidth + 8, FontManager.segoe16.getHeight() + 6, 2, new Color(0, 0, 0, Math.min((int) (this.signalHoveredAlpha * 255 + 0.5), 255)));
        FontManager.segoe14.drawString(this.status, x + width - 10 - statusWidth, offsetY + 32 + 2.5, RenderSystem.hexColor(0, 0, 0, (int) (this.signalHoveredAlpha * 150)));

        if (!inst.deleteMode) {
            Image.draw(new ResourceLocation("Zephyr/textures/multiplayer/edit.png"), x + width - 19, offsetY + height - 15, 11.5, 11, Image.Type.Normal);
        } else {
            RenderSystem.circle(x + width - 12, offsetY + height - 11, 6, selected ? RenderSystem.hexColor(0, 160, 255) : RenderSystem.hexColor(223, 223, 223, 223));
        }

//        Rect.draw(x + width - 24, offsetY + 13, 18, 14, 0xff0090ff, Rect.RectType.EXPAND);

        this.checkMouse(x, y, width, height, mouseX, mouseY, inst);
    }

    private void checkMouse(double x, double y, double width, double height, double mouseX, double mouseY, ZephyrMultiPlayerUI inst) {
        double offsetY = y - this.yAnimation;

        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, x, offsetY, width, height) && inst.dialog == null;
        if (hovered && Mouse.isButtonDown(0) && !mouseDown) {
            mouseDown = true;

            if (!inst.deleteMode) {

                boolean hoveredEdit = RenderSystem.isHovered(mouseX, mouseY, x + width - 18, offsetY + height - 15, 11.5, 11) && inst.dialog == null;

                if (!hoveredEdit) {
                    mc.displayGuiScreen(new GuiConnecting(inst, this.mc, server));
                } else {

                    inst.dialog = new ServerInfoDialog(server, inst.serverList.getServers().indexOf(server));
                }

            } else {
                selected = !selected;
            }
        }

        if (!Mouse.isButtonDown(0) && mouseDown)
            mouseDown = false;

    }

    private void checkServer(ZephyrMultiPlayerUI inst) {
        if (!this.server.pinged)
        {
            this.server.pinged = true;
            this.server.pingToServer = -2L;
            this.server.serverMOTD = "";
            this.server.populationInfo = "";
            inst.pingers.submit(() -> {
                try
                {
                    inst.oldServerPinger.ping(server);
                }
                catch (UnknownHostException var2)
                {
                    server.pingToServer = -1L;
                    server.serverMOTD = Formatting.DARK_RED + "Can't resolve hostname";
                }
                catch (Exception var3)
                {
                    server.pingToServer = -1L;
                    server.serverMOTD = Formatting.DARK_RED + "Can't connect to server.";
                }
            });
        }

        boolean clientOutdated = this.server.version > 47;
        boolean serverOutdated = this.server.version < 47;
        boolean clientOrServerOutdated = clientOutdated || serverOutdated;

        if (clientOrServerOutdated)
        {
            status = clientOutdated ? "Client out of date!" : "Server out of date!";
        }
        else if (this.server.pinged && this.server.pingToServer != -2L)
        {
            if (this.server.pingToServer < 0L)
            {
                signalStrength = 5;
            }
            else if (this.server.pingToServer < 150L)
            {
                signalStrength = 0;
            }
            else if (this.server.pingToServer < 300L)
            {
                signalStrength = 1;
            }
            else if (this.server.pingToServer < 600L)
            {
                signalStrength = 2;
            }
            else if (this.server.pingToServer < 1000L)
            {
                signalStrength = 3;
            }
            else
            {
                signalStrength = 4;
            }

            if (this.server.pingToServer < 0L)
            {
                signalStrength = -1;
                status = "(no connection)";
            }
            else
            {
                status = this.server.pingToServer + "ms";
            }
        }
        else
        {
            status = "Pinging...";
        }

        if (this.server.getBase64EncodedIconData() != null && !this.server.getBase64EncodedIconData().equals(this.base64EncodedIconData))
        {
            this.base64EncodedIconData = this.server.getBase64EncodedIconData();
            this.prepareServerIcon();
            inst.serverList.saveServerList();
        }
    }

    private void prepareServerIcon()
    {
        if (this.server.getBase64EncodedIconData() == null)
        {
            this.mc.getTextureManager().deleteTexture(this.serverIcon);
            this.serverTexture = null;
        }
        else
        {
            ByteBuf bytebuf = Unpooled.copiedBuffer(this.server.getBase64EncodedIconData(), Charsets.UTF_8);
            ByteBuf bytebuf1 = Base64.decode(bytebuf);
            BufferedImage bufferedimage;
            label101:
            {
                try
                {
                    bufferedimage = TextureUtil.readBufferedImage(new ByteBufInputStream(bytebuf1));
                    Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                    break label101;
                }
                catch (Throwable throwable)
                {
                    Minecraft.getLogger().error("Invalid icon for server " + this.server.serverName + " (" + this.server.serverIP + ")", throwable);
                    this.server.setBase64EncodedIconData(null);
                }
                finally
                {
                    bytebuf.release();
                    bytebuf1.release();
                }

                return;
            }

            if (this.serverTexture == null)
            {
                this.serverTexture = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
                this.mc.getTextureManager().loadTexture(this.serverIcon, this.serverTexture);
            }

            bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), this.serverTexture.getTextureData(), 0, bufferedimage.getWidth());
            this.serverTexture.updateDynamicTexture();
        }
    }

}
