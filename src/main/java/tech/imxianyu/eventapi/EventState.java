package tech.imxianyu.eventapi;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ImXianyu
 * @since 3/26/2023 7:34 AM
 */
public class EventState extends Event {

    @Getter
    @Setter
    State state = State.PRE;

    public boolean isPre() {
        return state == State.PRE;
    }
}
