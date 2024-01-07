package tech.imxianyu.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.imxianyu.eventapi.Event;

@AllArgsConstructor
public class MoveEvent extends Event {
    @Getter
    @Setter
    public double x, y, z;
}
