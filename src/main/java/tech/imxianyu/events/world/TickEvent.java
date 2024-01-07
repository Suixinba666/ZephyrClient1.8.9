package tech.imxianyu.events.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.imxianyu.eventapi.EventState;

/**
 * @author ImXianyu
 * @since 4/15/2023 7:40 PM
 */
@Getter
@AllArgsConstructor
public class TickEvent extends EventState {

    private int elapsedTicks;

    public TickEvent() {
        this.setParallel(true);
    }
}

