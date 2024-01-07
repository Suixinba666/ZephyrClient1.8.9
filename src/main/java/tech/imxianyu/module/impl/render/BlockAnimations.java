package tech.imxianyu.module.impl.render;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovingObjectPosition;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.render.blockanimations.*;
import tech.imxianyu.settings.NumberSetting;

/**
 * @author ImXianyu
 * @since 6/17/2023 9:48 PM
 */
public class BlockAnimations extends Module {

    @Handler
    public void onTick(TickEvent event) {
        this.setSuffix(this.getSubModes().getValue());
        if (mc.thePlayer == null || mc.theWorld == null)
            return;
        if (!event.isPre())
            return;
        if (mc.thePlayer.getItemInUseCount() > 0) {
            boolean mouseDown = mc.gameSettings.keyBindAttack.isKeyDown() && mc.gameSettings.keyBindUseItem.isKeyDown();
            if (mouseDown && !mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY) && mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK)) {
                mc.thePlayer.swingItem();
            }
        }
    };
    public NumberSetting<Double> x = new NumberSetting<>("X", 0.0, -1.0, 1.0, 0.05);
    public NumberSetting<Double> y = new NumberSetting<>("Y", 0.15, -1.0, 1.0, 0.05);
    public NumberSetting<Double> z = new NumberSetting<>("Z", 0.0, -1.0, 1.0, 0.05);

    public BlockAnimations() {
        super("Block Animations", Category.RENDER);
        super.addSubModules(new Vanilla(), new Remix(), new Lunar(), new Swing(), new Swong());
    }

    public void swingItem(EntityPlayerSP entityplayersp) {
        int swingAnimationEnd = entityplayersp.isPotionActive(Potion.digSpeed)
                ? (6 - (1 + entityplayersp.getActivePotionEffect(Potion.digSpeed).getAmplifier()))
                : (entityplayersp.isPotionActive(Potion.digSlowdown)
                ? (6 + (1 + entityplayersp.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2)
                : 6);
        if (!entityplayersp.isSwingInProgress || entityplayersp.swingProgressInt >= swingAnimationEnd / 2
                || entityplayersp.swingProgressInt < 0) {
            entityplayersp.swingProgressInt = -1;
            entityplayersp.isSwingInProgress = true;
            //mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        }
    }


}
