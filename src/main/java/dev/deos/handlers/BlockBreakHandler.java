package dev.deos.handlers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.deos.TravelAnchors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBreakHandler extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public BlockBreakHandler() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull BreakBlockEvent breakBlockEvent
    ) {
        Vector3i pos = breakBlockEvent.getTargetBlock();

        TravelAnchors.getStorage().remove(store.getExternalData().getWorld().getName(), pos);
    }

    @Nullable
    @Override
    public Query getQuery() {
        return Player.getComponentType();
    }

    @Nonnull
    @Override
    public Class<BreakBlockEvent> getEventType() {
        return BreakBlockEvent.class;
    }

}
