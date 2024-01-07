package tech.imxianyu.gui.clickgui.panel;

import lombok.Getter;

/**
 * @author ImXianyu
 * @since 5/2/2023 7:01 PM
 */
public abstract class Panel {

    @Getter
    private final String name;

    @Getter
    private final RenderValues renderValues = new RenderValues();

    public Panel(String name) {
        this.name = name;
    }

    public abstract void init();

    public abstract void draw(double posX, double posY, double width, double height, double mouseX, double mouseY, int dWheel);

    public boolean keyTyped(char typedChar, int keyCode) {
        return false;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {

    }

    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    public class RenderValues {

        public float hoveredAlpha = 0;

    }

}
