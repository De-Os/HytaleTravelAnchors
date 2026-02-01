package dev.deos.Interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.deos.TravelAnchors;
import dev.deos.TravelAnchorsStorage;
import dev.deos.UI.TravelAnchorListUI;
import dev.deos.handlers.MovementHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TravelAnchorUseInteraction extends SimpleBlockInteraction {

    public static final BuilderCodec<TravelAnchorUseInteraction> CODEC = BuilderCodec.builder(
            TravelAnchorUseInteraction.class, TravelAnchorUseInteraction::new
    ).build();

    @Override
    protected void interactWithBlock(
            @Nonnull World world,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InteractionType interactionType,
            @Nonnull InteractionContext interactionContext,
            @Nullable ItemStack itemStack,
            @Nonnull Vector3i pos,
            @Nonnull CooldownHandler cooldownHandler)
    {
        Ref<EntityStore> ref = interactionContext.getEntity();
        Store<EntityStore> store = ref.getStore();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());

        if (playerRef == null || player == null) {
            return;
        }

        world.execute(() -> {
            if (!MovementHandler.isTravelAnchor(world, pos.getX(), pos.getY(), pos.getZ())) {
                return;
            }

            TravelAnchorsStorage.TravelAnchorLocation loc = TravelAnchors.getStorage().get(world.getName(), pos);

            if (loc == null) {
                return;
            }

            player.getPageManager().openCustomPage(
                    ref,
                    store,
                    new TravelAnchorListUI(
                            playerRef,
                            CustomPageLifetime.CanDismissOrCloseThroughInteraction,
                            loc.anchorName == null ? "" : loc.anchorName,
                            loc.uuid,
                            TravelAnchors.getStorage().getAnchorsAround(
                                    new Vector3d(pos.getX(), pos.getY(), pos.getZ()),
                                    world.getName()
                            )
                    )
            );
        });
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nullable ItemStack itemStack, @Nonnull World world, @Nonnull Vector3i vector3i) {

    }
}
