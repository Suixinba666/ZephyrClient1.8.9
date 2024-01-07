package tech.imxianyu.events.player;

import lombok.AllArgsConstructor;
import tech.imxianyu.eventapi.Event;

@AllArgsConstructor
public class MoveEntityRotationEvent extends Event {
    public float rotationYaw;
}
