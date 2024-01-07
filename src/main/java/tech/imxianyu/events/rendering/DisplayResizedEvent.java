package tech.imxianyu.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.imxianyu.eventapi.Event;

@AllArgsConstructor
public class DisplayResizedEvent extends Event {
    @Getter
    private final int beforeWidth, beforeHeight, nowWidth, nowHeight;
}
