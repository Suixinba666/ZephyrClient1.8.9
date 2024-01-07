package tech.imxianyu.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.imxianyu.eventapi.EventCancellable;

/**
 * @author ImXianyu
 * @since 6/16/2023 3:17 PM
 */
@AllArgsConstructor
public class ChatEvent extends EventCancellable {

    @Getter
    @Setter
    private String msg;

    public ChatEvent() {
        this.setParallel(true);
    }

}
