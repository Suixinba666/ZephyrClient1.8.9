package tech.imxianyu.events.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.WorldClient;
import tech.imxianyu.eventapi.Event;

@RequiredArgsConstructor
public class WorldChangedEvent extends Event {
    @Getter
    private final WorldClient world;
}
