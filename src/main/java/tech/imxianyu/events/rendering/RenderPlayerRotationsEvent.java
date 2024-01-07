package tech.imxianyu.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.imxianyu.eventapi.Event;

@AllArgsConstructor
public class RenderPlayerRotationsEvent extends Event {

    @Getter
    @Setter
    public float rotationYaw, rotationPitch;
}
