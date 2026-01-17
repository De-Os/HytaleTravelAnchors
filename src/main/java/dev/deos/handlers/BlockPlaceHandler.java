package dev.deos.handlers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.deos.TravelAnchors;
import dev.deos.TravelAnchorsStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class BlockPlaceHandler extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    public BlockPlaceHandler() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull PlaceBlockEvent placeBlockEvent
    ) {
        Vector3i pos = placeBlockEvent.getTargetBlock();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            if (!MovementHandler.isTravelAnchor(world, pos.getX(), pos.getY(), pos.getZ())) {
                return;
            }


            TravelAnchorsStorage.TravelAnchorLocation newAnchorLocation = new TravelAnchorsStorage.TravelAnchorLocation(
                    world.getName(),
                    UUID.randomUUID().toString(),
                    "",
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            );

            TravelAnchors.getStorage().crateOrUpdateAnchor(newAnchorLocation);
        });
    }

    @Nullable
    @Override
    public Query getQuery() {
        return Player.getComponentType();
    }

    @Nonnull
    @Override
    public Class<PlaceBlockEvent> getEventType() {
        return PlaceBlockEvent.class;
    }
}
