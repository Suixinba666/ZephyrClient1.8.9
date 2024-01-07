package tech.imxianyu.module.impl.render;

import org.lwjglx.input.Keyboard;
import tech.imxianyu.gui.clickgui.ZephyrClickGui;
import tech.imxianyu.module.Module;

/**
 * @author ImXianyu
 * @since 5/1/2023 5:05 PM
 */
public class ClickGui extends Module {

    public ClickGui() {
        super("ClickGui", Category.RENDER);
        super.setKeyBind(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {

        if (mc.thePlayer != null && mc.currentScreen != ZephyrClickGui.getInstance())
            mc.displayGuiScreen(ZephyrClickGui.getInstance());

        this.toggle();

    }

}
