package tech.imxianyu.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import tech.imxianyu.eventapi.Event;

public class EntityMovementEvent extends Event {

    @Getter
    private final Entity movedEntity;

    @Getter
    private final double x, y, z;

    public EntityMovementEvent(Entity movedEntity, double x, double y, double z) {

        this.movedEntity = movedEntity;
        this.x = x;
        this.y = y;
        this.z = z;

        this.setParallel(true);
    }
}
