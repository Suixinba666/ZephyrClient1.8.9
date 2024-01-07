package tech.imxianyu.events.rendering;

import lombok.AllArgsConstructor;
import tech.imxianyu.eventapi.EventCancellable;

@AllArgsConstructor
public class BlockAnimationEvent extends EventCancellable {
    public float equipProgress, swingProgress, pitch, yaw;
}
