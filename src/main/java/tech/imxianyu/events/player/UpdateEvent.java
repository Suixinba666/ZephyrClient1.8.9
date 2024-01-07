package tech.imxianyu.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.imxianyu.eventapi.EventStateCancellable;

@AllArgsConstructor
public class UpdateEvent extends EventStateCancellable {
    @Getter
    @Setter
    private float rotationYaw, rotationPitch;

    @Getter
    @Setter
    private double posX, posY, posZ;

    @Getter
    @Setter
    private boolean onGround;
}
