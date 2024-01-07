package tech.imxianyu.rendering.entities.impl;

import lombok.Getter;
import lombok.Setter;
import tech.imxianyu.rendering.entities.RenderableEntity;
import tech.imxianyu.rendering.font.ZFontRenderer;

/**
 * @author ImXianyu
 * @since 4/24/2023 4:09 PM
 */
public class TextLabel extends RenderableEntity {

    @Getter
    @Setter
    private String text;

    @Getter
    @Setter
    private ZFontRenderer fontRenderer;

    @Getter
    @Setter
    private int color;

    public TextLabel(String text, ZFontRenderer fontRenderer, double x, double y, int color) {
        super(x, y, fontRenderer.getStringWidth(text), fontRenderer.getHeight());

        this.setText(text);
        this.setFontRenderer(fontRenderer);
        this.setColor(color);
    }

    public void setProperties(double x, double y, int color) {
        this.setX(x);
        this.setY(y);
        this.setColor(color);
    }


    public void draw() {
        this.fontRenderer.drawString(text, this.getX(), this.getY(), this.getColor());
        this.setWidth(this.fontRenderer.getStringWidth(text));
        this.setHeight(this.fontRenderer.getHeight());
    }
}
