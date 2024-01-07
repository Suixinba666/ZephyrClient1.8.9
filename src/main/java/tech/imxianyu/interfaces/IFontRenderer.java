package tech.imxianyu.interfaces;

/**
 * the *interworking* layer of the minecraft's font renderer and the client's font renderer
 * @author ImXianyu
 * @since 6/20/2023 9:50 AM
 */
public interface IFontRenderer {

    int drawString(String text, double x, double y, int color);

    int drawStringWithShadow(String text, double x, double y, int color);

    int getHeight();

    int getStringWidth(String text);

}
