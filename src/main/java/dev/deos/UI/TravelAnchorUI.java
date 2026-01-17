package dev.deos.UI;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.deos.TravelAnchors;
import dev.deos.TravelAnchorsStorage;

import javax.annotation.Nonnull;

public class TravelAnchorUI extends InteractiveCustomUIPage<TravelAnchorUI.TravelAnchorUIPageData> {
    private final String anchorName;
    private final String anchorUuid;

    public TravelAnchorUI(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, String anchorName, String anchorUuid) {
        super(playerRef, lifetime, TravelAnchorUIPageData.CODEC);
        this.anchorName = anchorName;
        this.anchorUuid = anchorUuid;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("TravelAnchorSettings.ui");
        uiCommandBuilder.set("#TasInput.Value", anchorName);

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SaveButton",
                EventData.of("ShouldSave", "true").append("@AnchorName", "#TasInput.Value"),
                false
        );

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BackButton",
                EventData.of("ShouldClose", "true"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull TravelAnchorUI.TravelAnchorUIPageData data) {
        super.handleDataEvent(ref, store, data);

        TravelAnchors.LOGGER.atInfo().log("Event: " + data.getAnchorName() + " close: " + data.shouldClose + " save: " + data.shouldSave);

        if (data.shouldSave() || data.shouldClose()) {
            if (data.shouldSave()) {
                TravelAnchorsStorage.TravelAnchorLocation travelAnchorLocation = TravelAnchors.getStorage().isTravelAnchorExists(anchorUuid);
                if (travelAnchorLocation != null) {
                    TravelAnchors.LOGGER.atInfo().log(travelAnchorLocation.toString());
                    travelAnchorLocation.anchorName = data.getAnchorName();
                    TravelAnchors.getStorage().crateOrUpdateAnchor(travelAnchorLocation);
                }
            }
            this.close();
            return;
        }

        sendUpdate();
    }

    public static class TravelAnchorUIPageData {
        public static final BuilderCodec<TravelAnchorUIPageData> CODEC = BuilderCodec
                .builder(TravelAnchorUIPageData.class, TravelAnchorUIPageData::new)
                .append(
                        new KeyedCodec<String>("@AnchorName", Codec.STRING),
                        (tauipd, val, extraInfo) -> tauipd.anchorName = val,
                        (tauipd, extraInfo) -> tauipd.anchorName).add()
                .append(
                        new KeyedCodec<String>("ShouldClose", Codec.STRING),
                        (tauipd, val, extraInfo) -> tauipd.shouldClose = val,
                        (tauipd, extraInfo) -> tauipd.anchorName).add()
                .append(
                        new KeyedCodec<String>("ShouldSave", Codec.STRING),
                        (tauipd, val, extraInfo) -> tauipd.shouldSave = val,
                        (tauipd, extraInfo) -> tauipd.anchorName).add()
                .build();

        private String anchorName = "";
        private String shouldClose = "";
        private String shouldSave = "";

        public String getAnchorName() {
            return anchorName;
        }

        public boolean shouldClose() {
            return shouldClose.equals("true");
        }

        public boolean shouldSave() {
            return shouldSave.equals("true");
        }
    }
}
