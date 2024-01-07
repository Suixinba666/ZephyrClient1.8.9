package tech.imxianyu.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;
import tech.imxianyu.eventapi.Event;

/**
 * @author ImXianyu
 * @since 4/15/2023 7:43 PM
 */
@Getter
@AllArgsConstructor
public class Render2DEvent extends Event {
    private final float partialTicks;
    private final ScaledResolution scaledResolution;

    public static class Render2DBeforeInventoryEvent extends Event {

    }
}
