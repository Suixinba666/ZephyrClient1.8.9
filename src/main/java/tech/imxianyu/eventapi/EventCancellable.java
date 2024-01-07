package tech.imxianyu.eventapi;

import lombok.Getter;

/**
 * @author ImXianyu
 * @since 3/25/2023 11:20 PM
 */
public class EventCancellable extends Event {

    @Getter
    boolean cancelled = false;

    public void setCancelled() {
        this.cancelled = true;
    }
}
