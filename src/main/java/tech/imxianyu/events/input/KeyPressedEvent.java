package tech.imxianyu.events.input;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tech.imxianyu.eventapi.Event;

/**
 * @author ImXianyu
 * @since 5/1/2023 5:50 PM
 */
@Getter
public class KeyPressedEvent extends Event {

    private final int keyCode;

    public KeyPressedEvent(int keyCode) {
        this.keyCode = keyCode;
    }

}
