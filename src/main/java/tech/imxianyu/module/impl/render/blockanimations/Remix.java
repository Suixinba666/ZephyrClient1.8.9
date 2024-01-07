package tech.imxianyu.module.impl.render.blockanimations;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.rendering.BlockAnimationEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.render.BlockAnimations;
import tech.imxianyu.module.submodule.SubModule;

/**
 * @author ImXianyu
 * @since 6/17/2023 9:53 PM
 */
public class Remix extends SubModule<BlockAnimations> {

    public Remix() {
        super("Remix");
    }
    @Handler
    public void onAnimation(BlockAnimationEvent event) {
        event.setCancelled();
        GlStateManager.translate(ModuleManager.blockAnimations.x.getValue(), ModuleManager.blockAnimations.y.getValue(), ModuleManager.blockAnimations.z.getValue());
        this.transformFirstPersonItem(event.equipProgress, 0.83f);
        this.doBlockTransformations();
        float remix = MathHelper.sin(MathHelper.sqrt_float(event.swingProgress) * 3.83f);
        GlStateManager.translate(-0.5f, 0.2f, 0.2f);
        GlStateManager.rotate(-remix * 0.0f, 0.0f, 0.0f, 0.0f);
        GlStateManager.rotate(-remix * 43.0f, 58.0f, 23.0f, 45.0f);
    };

    /**
     * Performs transformations prior to the rendering of a held item in first person.
     */
    private void transformFirstPersonItem(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    /**
     * Translate and rotate the render for holding a block
     */
    private void doBlockTransformations() {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

}
