package tech.imxianyu.gui.clickgui.panel.impl.musicpanel;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.music.QRCodeGenerator;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.BloomRenderer;

import java.awt.*;

/**
 * @author ImXianyu
 * @since 6/16/2023 4:05 PM
 */
public class LoginRenderer {

    @Getter
    public boolean closing = false;
    Thread loginThread;
    boolean success = false;
    float screeMaskAlpha = 0;
    double scale = 0;

    public LoginRenderer() {
        loginThread = new Thread(() -> {
            String cookie = CloudMusic.qrCodeLogin();
            System.out.println("Cookie is " + cookie);
            CloudMusic.api.setCookie(cookie);
            success = true;
            this.closing = true;
        });

        loginThread.start();
    }

    public void render(double mouseX, double mouseY, double posX, double posY, double width, double height) {
        screeMaskAlpha = AnimationSystem.interpolate(screeMaskAlpha * 255, this.isClosing() ? 0 : 120, 0.3f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
        scale = AnimationSystem.interpolate(scale, this.isClosing() ? 0 : 0.99999, 0.2);

        Rect.draw(posX, posY, width, height, RenderSystem.hexColor(0, 0, 0, (int) (screeMaskAlpha * 255)), Rect.RectType.EXPAND);

        GlStateManager.pushMatrix();
        RenderSystem.translateAndScale(posX + width / 2.0, posY + height / 2.0, scale);

        double pWidth = width / 2.0;
        double pHeight = height / 1.2;

        BloomRenderer.preRenderShadow();

        double x = posX + width / 2.0 - pWidth / 2.0;
        double y = posY + height / 2.0 - pHeight / 2.0;

        RoundedRect.drawRound(x, y, pWidth, pHeight, 5, new Color(43, 43, 43));

        BloomRenderer.postRenderShadow();

//        RenderSystem.doScissor((inta) x, (int) y, (int) pWidth, (int) pHeight);

        RoundedRect.drawRound(x, y, pWidth, pHeight, 5, new Color(43, 43, 43));

        double qWidth = 128, qHeight = 128;

        String[] strings = FontManager.pf40.fitWidth("Please scan the qr code to login.", pWidth - 16);

        double startY = posY + height / 6.0;

        for (String string : strings) {
            FontManager.pf40.drawCenteredString(string, posX + width / 2.0, startY, -1);
            startY += FontManager.pf40.getHeight();
        }

        Image.draw(QRCodeGenerator.qrCode, posX + width / 2.0 - qWidth / 2.0, posY + height / 6.0 * 4.0 - qHeight / 2.0, qWidth, qHeight, Image.Type.Normal);

        GlStateManager.popMatrix();
    }

    public boolean canClose() {
        return this.isClosing() && this.screeMaskAlpha <= 0.05 && success;
    }

}
