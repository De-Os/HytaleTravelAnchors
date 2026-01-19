package dev.deos.UI;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.deos.TravelAnchors;
import dev.deos.TravelAnchorsStorage;
import dev.deos.handlers.MovementHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class TravelAnchorListUI extends InteractiveCustomUIPage<TravelAnchorListUI.TravelAnchorUIPageData> {
    private final String anchorUuid;
    private String anchorName;
    private final List<TravelAnchorsStorage.TravelAnchorLocation> nearbyAnchors;

    public TravelAnchorListUI(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, String anchorName, String anchorUuid, List<TravelAnchorsStorage.TravelAnchorLocation> nearbyAnchors) {
        super(playerRef, lifetime, TravelAnchorUIPageData.CODEC);
        this.anchorName = anchorName;
        this.anchorUuid = anchorUuid;
        this.nearbyAnchors = nearbyAnchors;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("TravelAnchorsList.ui");

        uiCommandBuilder.set("#TasInput.Value", anchorName);

        boolean noNearbyAnchors = true;

        int i = 0;
        for (TravelAnchorsStorage.TravelAnchorLocation location : nearbyAnchors) {
            if (location.anchorName.isEmpty() || location.uuid.equals(anchorUuid)) {
                continue;
            }

            noNearbyAnchors = false;

            String buttonTextKey = "#TaList[" + i + "] #AnchorName.Text";

            uiCommandBuilder.append("#TaList", "TravelAnchorsListButton.ui");
            uiCommandBuilder.set(buttonTextKey, location.anchorName);
            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#TaList[" + i + "] #AnchorName",
                    EventData.of("AnchorUUID", location.uuid).append("ShouldClose", "true"),
                    false
            );

            i++;
        }

        if (noNearbyAnchors) {
            uiCommandBuilder.set("#NoNearbyAnchorsLabel.Visible", true);
        }

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ReturnButton",
                EventData.of("ShouldReturn", "true"),
                false
        );

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SaveButton",
                EventData.of("ShouldSave", "true").append("@AnchorName", "#TasInput.Value").append("ShouldReturn", "true"),
                false
        );


        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BackButton",
                EventData.of("ShouldClose", "true"),
                false
        );

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SettingsButton",
                EventData.of("ShouldOpenSettings", "true"),
                false
        );

        updateRenameHintVisibility();
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull TravelAnchorListUI.TravelAnchorUIPageData data) {
        super.handleDataEvent(ref, store, data);

        TravelAnchors.LOGGER.atInfo().log("Got event: " + data);

        if (!data.getAnchorUUID().isEmpty()) {
            TravelAnchorsStorage.TravelAnchorLocation travelAnchor = TravelAnchors.getStorage().isTravelAnchorExists(data.getAnchorUUID());

            World world = store.getExternalData().getWorld();

            world.execute(() -> {
                (new MovementHandler()).teleport(
                        ref,
                        world,
                        store,
                        (int) travelAnchor.x, (int) travelAnchor.y, (int) travelAnchor.z,
                        store.getComponent(ref, PlayerRef.getComponentType()).getHeadRotation(),
                        playerRef.getUsername()
                );
            });


            this.close();
            this.sendUpdate();
            return;
        }

        if (data.shouldOpenSettings()) {
            UICommandBuilder cmd = new UICommandBuilder();
            cmd.set("#TalPanel.Visible", false);
            cmd.set("#TasPanel.Visible", true);
            this.sendUpdate(cmd);
            return;
        }

        if (data.shouldSave()) {
            TravelAnchorsStorage.TravelAnchorLocation travelAnchorLocation = TravelAnchors.getStorage().isTravelAnchorExists(anchorUuid);
            if (travelAnchorLocation != null) {
                travelAnchorLocation.anchorName = data.getAnchorName();
                TravelAnchors.getStorage().crateOrUpdateAnchor(travelAnchorLocation);
                anchorName = data.getAnchorName();

                updateRenameHintVisibility();
            }
            data.shouldReturn = "true";
        }

        if (data.shouldReturn()) {
            UICommandBuilder cmd = new UICommandBuilder();
            cmd.set("#TasPanel.Visible", false);
            cmd.set("#TalPanel.Visible", true);
            this.sendUpdate(cmd);
            return;
        }

        if (data.shouldClose()) {
            this.close();
            this.sendUpdate();
            return;
        }

        sendUpdate();
    }

    private void updateRenameHintVisibility() {
        boolean visibility = anchorName == null || anchorName.isEmpty();

        UICommandBuilder cmd = new UICommandBuilder();
        cmd.set("#RenameCurrentLabel.Visible", visibility);
        cmd.set("#RenameCurrentLabelSecondary.Visible", visibility);
        cmd.set("#RenameCurrentLabelThird.Visible", visibility);
        this.sendUpdate(cmd);
    }

    public static class TravelAnchorUIPageData {
        public static final BuilderCodec<TravelAnchorUIPageData> CODEC = BuilderCodec
                .builder(TravelAnchorUIPageData.class, TravelAnchorUIPageData::new)
                .append(
                        new KeyedCodec<String>("@AnchorName", Codec.STRING),
                        (tasuipd, val, extraInfo) -> tasuipd.anchorName = val,
                        (tasuipd, extraInfo) -> tasuipd.anchorName).add()
                .append(
                        new KeyedCodec<String>("AnchorUUID", Codec.STRING),
                        (tauipd, val, extraInfo) -> tauipd.anchorUuid = val,
                        (tauipd, extraInfo) -> tauipd.anchorUuid).add()
                .append(
                        new KeyedCodec<String>("ShouldClose", Codec.STRING),
                        (tauipd, val, extraInfo) -> tauipd.shouldClose = val,
                        (tauipd, extraInfo) -> tauipd.shouldClose).add()
                .append(
                        new KeyedCodec<String>("ShouldOpenSettings", Codec.STRING),
                        (tauipd, val, extraInfo) -> tauipd.shouldOpenSettings = val,
                        (tauipd, extraInfo) -> tauipd.shouldOpenSettings).add()
                .append(
                        new KeyedCodec<String>("ShouldReturn", Codec.STRING),
                        (tauipd, val, extraInfo) -> tauipd.shouldReturn = val,
                        (tauipd, extraInfo) -> tauipd.shouldReturn).add()
                .append(
                        new KeyedCodec<String>("ShouldSave", Codec.STRING),
                        (tauipd, val, extraInfo) -> tauipd.shouldSave = val,
                        (tauipd, extraInfo) -> tauipd.shouldSave).add()
                .build();

        private String anchorName = "";
        private String anchorUuid = "";
        private String shouldClose = "";
        private String shouldOpenSettings = "";
        private String shouldReturn = "";
        private String shouldSave = "";

        public String getAnchorName() {
            return anchorName;
        }

        public String getAnchorUUID() {
            return anchorUuid;
        }

        public boolean shouldClose() {
            return shouldClose.equals("true");
        }

        public boolean shouldOpenSettings() {
            return shouldOpenSettings.equals("true");
        }

        public boolean shouldReturn() {
            return shouldReturn.equals("true");
        }

        public boolean shouldSave() {
            return shouldSave.equals("true");
        }

        @Override
        public String toString() {
            return "TravelAnchorUIPageData{anchorUUID=" + anchorUuid + ", anchorName" + anchorName + ", shouldClose=" + shouldClose + ", shouldOpenSettings=" + shouldOpenSettings + ", shouldReturn = " + shouldReturn + ", shouldSave=" + shouldSave + "}";
        }
    }
}
