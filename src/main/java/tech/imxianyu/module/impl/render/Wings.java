package tech.imxianyu.module.impl.render;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.RenderWings;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;

public class Wings
        extends Module {
    public NumberSetting<Double> scale = new NumberSetting<>("Scale", 1.0, 0.0, 2.5, 0.1);
    public BooleanSetting firstPerson = new BooleanSetting("Render in first person", false);
    RenderWings wings = new RenderWings();
    @Handler
    public void onRender3D(Render3DEvent event) {
        if (!mc.thePlayer.isInvisible()) {
            this.wings.renderWings(mc.thePlayer, event.partialTicks);
        }
    };

    public Wings() {
        super("Wings", Category.RENDER);
    }
}

