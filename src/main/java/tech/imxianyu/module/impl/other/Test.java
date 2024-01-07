package tech.imxianyu.module.impl.other;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;

/**
 * @author ImXianyu
 * @since 6/4/2023 5:12 PM
 */
public class Test extends Module {

    public BooleanSetting bs = new BooleanSetting("Boolean", true);
    public NumberSetting<Double> ns = new NumberSetting<>("Number", 1.0, 0.0, 2.0, 0.1);
    public ModeSetting<Modes> mode = new ModeSetting<>("Mode", Modes.Mode01);
    public ModeSetting<Modes> mode2 = new ModeSetting<>("Mode2", Modes.Mode01);
    public ModeSetting<Modes> mode3 = new ModeSetting<>("Mode3", Modes.Mode01);
    public NumberSetting<Double> ns2 = new NumberSetting<>("Number2", 1.0, 0.0, 2.0, 0.1);
    public BooleanSetting bs2 = new BooleanSetting("Boolean2", true);
    public Test() {
        super("Test", Category.OTHER);

    }

    @Override
    public void onEnable() {
//        mc.thePlayer.posY += 10;
    }

    @Handler
    private void onRender2D(Render2DEvent event) {
    }

    public enum Modes {
        Mode01,
        Mode02,
        Mode03
    }
}
