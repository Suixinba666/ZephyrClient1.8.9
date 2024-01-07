package tech.imxianyu.management;

import lombok.SneakyThrows;
import tech.imxianyu.interfaces.AbstractManager;
import tech.imxianyu.rendering.font.ZFontRenderer;

import java.awt.*;
import java.util.Objects;

/**
 * @author ImXianyu
 * @since 4/8/2023 11:09 AM
 */
public class FontManager extends AbstractManager {

    public static ZFontRenderer pf12, pf14, pf16, pf18, pf20, pf20bold, pf25, pf26, pf25bold, pf36, pf40;
    public static ZFontRenderer gsans25, gsans40;
    public static ZFontRenderer segoe14, segoe16, segoe18, segoe20, segoe38;
    public static ZFontRenderer baloo18;
    public static ZFontRenderer icon16, icon18, icon25, icon30;
    public static ZFontRenderer pf24Semibold, pf14Semibold, pf40Medium;
    public static ZFontRenderer sfPro40Medium;


    public FontManager() {
        super();
    }

    public static void loadFonts() {
        pf25 = new ZFontRenderer(fontFromTTF("pf_normal.ttf", 25, Font.PLAIN), true, true, true, 0, 1);
        pf25bold = new ZFontRenderer(fontFromTTF("pf_middlebold.ttf", 25, Font.PLAIN), true, true, true, 0, 1);

        pf12 = new ZFontRenderer(fontFromTTF("pf_normal.ttf", 12, Font.PLAIN), true, true, true, 0, 1);
        pf14 = new ZFontRenderer(fontFromTTF("pf_normal.ttf", 14, Font.PLAIN), true, true, true, 0, 1);
        pf16 = new ZFontRenderer(fontFromTTF("pf_normal.ttf", 16, Font.PLAIN), true, true, true, 0, 1);
        pf18 = new ZFontRenderer(fontFromTTF("pf_normal.ttf", 18, Font.PLAIN), true, true, true, 0, 1);
        pf20 = new ZFontRenderer(fontFromTTF("pf_normal.ttf", 20, Font.PLAIN), true, true, true, 0, 1);
        pf20bold = new ZFontRenderer(fontFromTTF("pf_middlebold.ttf", 20, Font.PLAIN), true, true, false, 0, 1);
        pf26 = new ZFontRenderer(fontFromTTF("pf_normal.ttf", 26, Font.PLAIN), true, true, true, 0, 1);
        pf36 = new ZFontRenderer(fontFromTTF("pf_normal.ttf", 36, Font.PLAIN), true, true, true, 0, 1);
        pf40 = new ZFontRenderer(fontFromTTF("pf_normal.ttf", 40, Font.PLAIN), true, true, false, 0, 1);
        baloo18 = new ZFontRenderer(fontFromTTF("Baloo.ttf", 18, Font.PLAIN), true, true, false, 0, 1);
        segoe14 = new ZFontRenderer(fontFromTTF("segoeui.ttf", 14, Font.PLAIN), true, true, true, 0, 0);
        segoe16 = new ZFontRenderer(fontFromTTF("segoeui.ttf", 16, Font.PLAIN), true, true, true, 0, 0);
        segoe18 = new ZFontRenderer(fontFromTTF("segoeui.ttf", 18, Font.PLAIN), true, true, false, 0, 0);
        segoe20 = new ZFontRenderer(fontFromTTF("segoeui.ttf", 20, Font.PLAIN), true, true, true, 0, 0);
        segoe38 = new ZFontRenderer(fontFromTTF("segoeui.ttf", 38, Font.PLAIN), true, true, false, 0, 0);
        gsans25 = new ZFontRenderer(fontFromTTF("GoogleSans.ttf", 25, Font.PLAIN), true, true, false, 0, 1);
        gsans40 = new ZFontRenderer(fontFromTTF("GoogleSans.ttf", 40, Font.PLAIN), true, true, false, 0, 1);
        icon16 = new ZFontRenderer(fontFromTTF("icon.ttf", 16, Font.PLAIN), true, true, false, 0, 0);
        icon18 = new ZFontRenderer(fontFromTTF("icon.ttf", 18, Font.PLAIN), true, true, false, 0, 0);
        icon25 = new ZFontRenderer(fontFromTTF("icon.ttf", 25, Font.PLAIN), true, true, false, 0, 0);
        icon30 = new ZFontRenderer(fontFromTTF("icon.ttf", 30, Font.PLAIN), true, true, false, 0, 0);

        pf24Semibold = new ZFontRenderer(fontFromTTF("pf_semibold.ttf", 24, Font.PLAIN), true, true, true, 0, 0);
        pf14Semibold = new ZFontRenderer(fontFromTTF("pf_semibold.ttf", 14, Font.PLAIN), true, true, true, 0, 0);
        pf40Medium = new ZFontRenderer(fontFromTTF("pf_middlebold.ttf", 40, Font.PLAIN), true, true, true, 0, 0);
        sfPro40Medium = new ZFontRenderer(fontFromTTF("SF-Pro-Text-Semibold.otf", 40, Font.PLAIN), true, true, false, 0, 0);
    }

    @SneakyThrows
    public static Font fontFromTTF(String fontName, float fontSize, int fontType) {
        Font font = Font.createFont(Font.TRUETYPE_FONT,
                Objects.requireNonNull(FontManager.class.getResourceAsStream("/assets/minecraft/Zephyr/fonts/" + fontName)));
        font = font.deriveFont(fontType, fontSize);
        return font;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }
}
