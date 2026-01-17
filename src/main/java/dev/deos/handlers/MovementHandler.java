package dev.deos.handlers;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.deos.TravelAnchors;
import dev.deos.TravelAnchorsStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MovementHandler extends EntityTickingSystem<EntityStore> {
    public static final int MAX_WORLD_HEIGHT_Y = 320;

    private final ComponentType<EntityStore, MovementStatesComponent> movementType;
    private final ComponentType<EntityStore, TransformComponent> transformType;

    public MovementHandler() {
        this.movementType = MovementStatesComponent.getComponentType();
        this.transformType = TransformComponent.getComponentType();
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        Ref<EntityStore> entityStore = archetypeChunk.getReferenceTo(index);

        PlayerRef playerRef = commandBuffer.getComponent(entityStore, PlayerRef.getComponentType());

        if (playerRef == null) {
            return;
        }

        MovementStatesComponent movementStatesComponent = archetypeChunk.getComponent(index, movementType);
        if (movementStatesComponent == null) {
            return;
        }

        Vector3d position = playerRef.getTransform().getPosition();

        int playerX = (int) Math.floor(position.getX());
        int playerY = (int) Math.floor(position.getY());
        int playerZ = (int) Math.floor(position.getZ());

        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            if (!isTravelAnchor(world, playerX, playerY - 1, playerZ)) {
                return;
            }

            if (movementStatesComponent.getMovementStates().jumping || movementStatesComponent.getMovementStates().crouching) {
                int addition = movementStatesComponent.getMovementStates().crouching ? -1 : 1;
                for (int tempY = playerY + addition * 2; (tempY <= MAX_WORLD_HEIGHT_Y && tempY >= 0); tempY += addition) {
                    if (isTravelAnchor(world, playerX, tempY, playerZ)) {
                        if (teleport(
                                entityStore,
                                world,
                                store,
                                playerX, tempY, playerZ
                        )) {
                            return;
                        }
                    }
                }
            }

            if (movementStatesComponent.getMovementStates().crouching) {
                return;
            }

            // 1.8 ~~ eyes height
            var positionVec = position.add(0, 1.8, 0);

            List<TravelAnchorsStorage.TravelAnchorLocation> anchorsAround = TravelAnchors.getStorage().getAnchorsAround(positionVec, world.getName());
            if (anchorsAround.isEmpty()) {
                return;
            }

        });
    }

    public boolean canTeleportTo(
            World world,
            int x, int y, int z
    ) {
        return !isObstructed(world, x, y, z);
    }

    private boolean teleport(
            Ref<EntityStore> entityStore,
            World world,
            Store<EntityStore> store,
            int x, int y, int z
    ) {
        TravelAnchors.LOGGER.atInfo().log("Teleporting to + x = " + x + ", y = " + y + ", z = " + z);

        if (!canTeleportTo(world, x, y, z)) {
            return false;
        }

        Teleport teleport = new Teleport(new Transform(x + 0.5, y + 1, z + 0.5));

        world.execute(() -> {
            store.addComponent(entityStore, Teleport.getComponentType(), teleport);
        });

        return true;
    }

    public static boolean isTravelAnchor(World world, int x, int y, int z) {
        BlockType blockType = world.getBlockType(x, y, z);
        return
                blockType != null
                        && blockType.getId().equalsIgnoreCase("travel_anchor")
                        && blockType.getGroup().equalsIgnoreCase("travelanchors");
    }

    private boolean isObstructed(World world, int x, int y, int z) {
        for (int tempY = y + 1; (tempY <= MAX_WORLD_HEIGHT_Y && tempY <= y + 2); tempY++) {
            BlockType blockType = world.getBlockType(x, tempY, z);
            if (blockType != null && !blockType.getId().equalsIgnoreCase("empty")) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), movementType, transformType);
    }
}
