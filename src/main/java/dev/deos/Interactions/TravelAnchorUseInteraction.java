package dev.deos.Interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class TravelAnchorUseInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<TravelAnchorUseInteraction> CODEC = BuilderCodec.builder(
            TravelAnchorUseInteraction.class, TravelAnchorUseInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(
            @NonNullDecl InteractionType interactionType,
            @NonNullDecl InteractionContext interactionContext,
            @NonNullDecl CooldownHandler cooldownHandler
    ) {
    }
}
