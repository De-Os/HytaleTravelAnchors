package dev.deos;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;
import dev.deos.Interactions.TravelAnchorUseInteraction;
import dev.deos.handlers.BlockBreakHandler;
import dev.deos.handlers.BlockPlaceHandler;
import dev.deos.handlers.MovementHandler;
import dev.deos.handlers.PreUseBlockEventHandler;

import javax.annotation.Nonnull;

public class TravelAnchors extends JavaPlugin {

    private static Config<TravelAnchorsConfig> config;
    private static TravelAnchorsStorage storage;

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public TravelAnchors(@Nonnull JavaPluginInit init) {
        super(init);

        config = this.withConfig("TravelAnchors", TravelAnchorsConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();

        this.getEntityStoreRegistry().registerSystem(new MovementHandler());
        this.getEntityStoreRegistry().registerSystem(new BlockPlaceHandler());
        this.getEntityStoreRegistry().registerSystem(new BlockBreakHandler());
        this.getEntityStoreRegistry().registerSystem(new PreUseBlockEventHandler());

        this.getCodecRegistry(Interaction.CODEC).register(
                "Travel_Anchor_Use_Interaction",
                TravelAnchorUseInteraction.class,
                TravelAnchorUseInteraction.CODEC
        );
        TravelAnchors.config.save();
    }

    protected void start(){
        TravelAnchors.storage = new TravelAnchorsStorage(Universe.get().getPath());
    }

    protected void shutdown() {
        if(TravelAnchors.storage != null){
            TravelAnchors.storage.save();
            TravelAnchors.LOGGER.atInfo().log("Saved anchors: " + TravelAnchorsStorage.anchorLocationMap.size());
        }
    }

    public static TravelAnchorsConfig getConfig() {
        return TravelAnchors.config.get();
    }

    public static TravelAnchorsStorage getStorage() {
        return TravelAnchors.storage;
    }
}