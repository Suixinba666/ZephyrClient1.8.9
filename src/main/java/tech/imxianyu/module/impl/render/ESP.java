package tech.imxianyu.module.impl.render;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.ESP2D;
import tech.imxianyu.rendering.HSBColor;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ColorSetting;
import tech.imxianyu.settings.ModeSetting;

/**
 * @author ImXianyu
 * @since 2022/7/17 9:10
 */
public class ESP extends Module {

    public ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Shader);
    @Handler
    public void onTick(TickEvent event) {
        this.setSuffix(this.mode.getCurMode());
    };
    @Handler
    public void onRender3D(Render3DEvent event) {
        if (this.mode.getValue() == Mode.Flat) {
            ESP2D.INSTANCE.updatePositions(mc);
        }
    };
    public ColorSetting color = new ColorSetting("Color", new HSBColor(255, 255, 255, 255));
    public BooleanSetting invisible = new BooleanSetting("Invisible", true);
    public BooleanSetting box = new BooleanSetting("Box", true, () -> this.mode.getValue() == Mode.Flat);
    public BooleanSetting tags = new BooleanSetting("Tags", true, () -> this.mode.getValue() == Mode.Flat);
    public BooleanSetting armor = new BooleanSetting("Armor", true, () -> this.mode.getValue() == Mode.Flat);
    public BooleanSetting health = new BooleanSetting("Health", true, () -> this.mode.getValue() == Mode.Flat);
    public ModeSetting<HealthColorMode> healthColorMode = new ModeSetting<>("Health Color Mode", HealthColorMode.Static, () -> health.getValue() && this.mode.getValue() == Mode.Flat);
    @Handler
    public void onRender2D(Render2DEvent event) {
        if (this.mode.getValue() == Mode.Flat) {
            ESP2D.INSTANCE.renderBox(mc);
        }
    };

    public ESP() {
        super("ESP", Category.RENDER);
    }

    public enum Mode {
        Shader,
        Flat
    }

    public enum HealthColorMode {
        Static,
        Health,
        HPBar
    }
}
