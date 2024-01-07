package tech.imxianyu.events.rendering;

import lombok.Getter;
import lombok.Setter;
import tech.imxianyu.eventapi.EventCancellable;

/**
 * @author ImXianyu
 * @since 4/8/2023 11:19 AM
 */
public class TextRenderEvent extends EventCancellable {

    @Getter
    @Setter
    private String text;

    public TextRenderEvent(String text) {
        this.text = text;
    }
}
