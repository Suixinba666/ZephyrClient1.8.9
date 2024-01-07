package tech.imxianyu.events.input;

import lombok.AllArgsConstructor;
import tech.imxianyu.eventapi.Event;

@AllArgsConstructor
public class MouseXYChangeEvent extends Event {
    public int deltaX, deltaY;
}
