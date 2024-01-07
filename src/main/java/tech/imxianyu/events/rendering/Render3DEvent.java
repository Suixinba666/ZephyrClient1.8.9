package tech.imxianyu.events.rendering;

import lombok.AllArgsConstructor;
import tech.imxianyu.eventapi.Event;

@AllArgsConstructor
public class Render3DEvent extends Event {
    public final float partialTicks;
}
