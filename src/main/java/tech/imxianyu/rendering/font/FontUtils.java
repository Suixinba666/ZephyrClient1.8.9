package tech.imxianyu.rendering.font;

import tech.imxianyu.management.FontManager;

import java.util.Arrays;
import java.util.List;

public class FontUtils {

    private static final List<ZFontRenderer> fontRenderers = Arrays.asList(
            FontManager.pf40,
            FontManager.pf36,
            FontManager.pf25,
            FontManager.pf20,
            FontManager.pf18,
            FontManager.pf16,
            FontManager.pf14,
            FontManager.pf12
    );

    public static ZFontRenderer getFontRendererByWidth(String str, double width) {
        for (ZFontRenderer fontRenderer : fontRenderers) {
            if (fontRenderer.getStringWidth(str) <= width)
                return fontRenderer;
        }

        return FontManager.pf12;
    }

}
