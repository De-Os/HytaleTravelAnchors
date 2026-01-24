package dev.deos.handlers;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.BlockInteractionUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.deos.TravelAnchors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class MovementHandler extends EntityTickingSystem<EntityStore> {
    public static final int MAX_WORLD_HEIGHT_Y = 320;
    public static final int TELEPORT_COOLDOWN_MS = 300;

    public static HashMap<String, Long> cooldowns = new HashMap<>();


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

        if(isOnCooldown(playerRef.getUsername())){
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

        MovementStates movementStates = movementStatesComponent.getMovementStates();

        if (!movementStates.jumping && !movementStates.crouching) {
            return;
        }

        world.execute(() -> {
            if (!isTravelAnchor(world, playerX, playerY - 1, playerZ)) {
                return;
            }

            int addition = movementStates.crouching ? -1 : 1;
            for (int tempY = playerY + addition * 2; (tempY <= MAX_WORLD_HEIGHT_Y && tempY >= 0); tempY += addition) {
                if (isTravelAnchor(world, playerX, tempY, playerZ)) {
                    if (teleport(
                            entityStore,
                            world,
                            store,
                            playerX, tempY, playerZ,
                            playerRef.getHeadRotation(),
                            playerRef.getUsername()
                    )) {
                        if(movementStates.crouching){
                            movementStates.crouching = false;
                        }
                        if(movementStates.jumping){
                            movementStates.jumping = false;
                        }
                        return;
                    }
                }
            }
        });
    }

    public boolean canTeleportTo(
            World world,
            int x, int y, int z
    ) {
        return !isObstructed(world, x, y, z);
    }

    public boolean teleport(
            Ref<EntityStore> entityStore,
            World world,
            Store<EntityStore> store,
            int x, int y, int z,
            Vector3f rotation,
            String username
    ) {
        if (!canTeleportTo(world, x, y, z)) {
            return false;
        }

        setCooldown(username);

        Teleport teleport = Teleport.createForPlayer(world, new Vector3d(x + 0.5, y + 1, z + 0.5), rotation);

        world.execute(() -> {
            store.addComponent(entityStore, Teleport.getComponentType(), teleport);
        });

        return true;
    }

    public static boolean isOnCooldown(String username){
        return cooldowns.containsKey(username) && cooldowns.get(username) >= System.currentTimeMillis();
    }

    public static void setCooldown(String username){
        cooldowns.put(username, System.currentTimeMillis() + (long) TELEPORT_COOLDOWN_MS);
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
