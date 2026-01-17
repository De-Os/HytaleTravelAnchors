package dev.deos.handlers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.deos.TravelAnchors;
import dev.deos.TravelAnchorsStorage;
import dev.deos.UI.TravelAnchorUI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PreUseBlockEventHandler extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public PreUseBlockEventHandler() {
        super(UseBlockEvent.Pre.class);
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull UseBlockEvent.Pre useBlockEvent
    ) {
        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());

        if (playerRef == null || player == null) {
            return;
        }

        Vector3i pos = useBlockEvent.getTargetBlock();

        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            if (!MovementHandler.isTravelAnchor(world, pos.getX(), pos.getY(), pos.getZ())) {
                return;
            }

            useBlockEvent.setCancelled(true);

            TravelAnchorsStorage.TravelAnchorLocation loc = TravelAnchors.getStorage().get(world.getName(), pos);

            if (loc == null) {
                return;
            }

            player.getPageManager().openCustomPage(
                    playerRef.getReference(),
                    store,
                    new TravelAnchorUI(
                            playerRef,
                            CustomPageLifetime.CanDismissOrCloseThroughInteraction,
                            loc.anchorName == null ? "" : loc.anchorName,
                            loc.uuid
                    )
            );
        });
    }

    @Nullable
    @Override
    public Query getQuery() {
        return Player.getComponentType();
    }

    @Nonnull
    @Override
    public Class<UseBlockEvent.Pre> getEventType() {
        return UseBlockEvent.Pre.class;
    }
}
