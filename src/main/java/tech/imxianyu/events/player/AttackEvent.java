package tech.imxianyu.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import tech.imxianyu.eventapi.EventCancellable;

@AllArgsConstructor
public class AttackEvent extends EventCancellable {
    @Getter
    @Setter
    private Entity attackedEntity;
}
