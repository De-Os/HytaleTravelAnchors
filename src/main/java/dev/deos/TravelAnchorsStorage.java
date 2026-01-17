package dev.deos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TravelAnchorsStorage {
    private static final String STORAGE_FILE = "travel_anchors.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Type TRAVEL_ANCHORS_TYPE = new TypeToken<List<TravelAnchorLocation>>() {
    }.getType();
    public static List<TravelAnchorLocation> anchorLocationMap = new ArrayList<>();

    private final Path dataDirectory;

    public TravelAnchorsStorage(@Nonnull Path path) {
        this.dataDirectory = path;

        this.load();
    }

    private void load() {
        Path file = this.dataDirectory.resolve(STORAGE_FILE);
        if (!Files.exists(file)) {
            TravelAnchors.LOGGER.atInfo().log("No travel anchors data file found.");
            return;
        }

        try {
            String string = Files.readString(file);

            anchorLocationMap = GSON.fromJson(string, TRAVEL_ANCHORS_TYPE);
        } catch (IOException e) {
            TravelAnchors.LOGGER.atWarning().log("Error reading travel anchors data file:");
            TravelAnchors.LOGGER.atWarning().log(e.toString());
        }
    }

    public void save() {
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                TravelAnchors.LOGGER.atSevere().log("Unable to create travel anchors data directory");
                TravelAnchors.LOGGER.atSevere().log(e.toString());
                return;
            }
        }

        Path path = this.dataDirectory.resolve(STORAGE_FILE);
        String string = GSON.toJson(anchorLocationMap, TRAVEL_ANCHORS_TYPE);
        try {
            Files.writeString(path, string);
            TravelAnchors.LOGGER.atInfo().log("Travel anchors data saved");
        } catch (IOException e) {
            TravelAnchors.LOGGER.atSevere().log("Unable to save travel anchors data");
            TravelAnchors.LOGGER.atSevere().log(e.toString());
        }
    }

    public void crateOrUpdateAnchor(TravelAnchorLocation location) {
        TravelAnchorLocation existing = isTravelAnchorExists(location.uuid);

        if (existing != null) {
            anchorLocationMap.remove(existing);
        }

        anchorLocationMap.add(location);

        save();
    }

    public void remove(TravelAnchorLocation location) {
        TravelAnchorLocation existing = isTravelAnchorExists(location.uuid);

        if (existing != null) {
            anchorLocationMap.remove(existing);
        }

        save();
    }

    public void remove(String world, int x, int y, int z) {
        TravelAnchorLocation existing = get(world, x, y, z);

        if (existing == null) {
            return;
        }

        anchorLocationMap.remove(existing);
        save();
    }

    public void remove(String world, Vector3i pos) {
        remove(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public TravelAnchorLocation get(String world, int x, int y, int z) {
        List<TravelAnchorLocation> existing = anchorLocationMap
                .stream().filter(p -> Objects.equals(p.worldName, world) && p.x == x && p.y == y && p.z == z)
                .toList();
        if (existing.isEmpty()) {
            return null;
        }

        return existing.getFirst();
    }

    public TravelAnchorLocation get(String world, Vector3i pos) {
        return get(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public List<TravelAnchorLocation> getAnchorsAround(Vector3d pos, String worldName) {
        int maxDistance = TravelAnchors.getConfig().maximumDistance;
        double maxDistSq = Math.pow(maxDistance, 2);
        return anchorLocationMap
                .stream().filter(p -> distToCenterSqr(p, pos.getX(), pos.getY(), pos.getZ()) < maxDistSq)
                .toList();
    }

    public TravelAnchorLocation isTravelAnchorExists(String uuid) {
        List<TravelAnchorLocation> filtered = anchorLocationMap
                .stream().filter(p -> p.uuid.equals(uuid))
                .toList();

        if (filtered.isEmpty()) {
            return null;
        }

        return filtered.getFirst();
    }

    private double distToCenterSqr(TravelAnchorLocation location, double x, double y, double z) {
        double d0 = location.x + (double) 0.5F - x;
        double d1 = location.y + (double) 0.5F - y;
        double d2 = location.z + (double) 0.5F - z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public static class TravelAnchorLocation {
        public String worldName;
        public String uuid;
        public String anchorName;
        public double x;
        public double y;
        public double z;

        public TravelAnchorLocation(
                @Nonnull String worldName,
                @Nonnull String uuid,
                String anchorName,
                double x,
                double y,
                double z
        ) {
            this.worldName = worldName;
            this.uuid = uuid;
            this.anchorName = anchorName;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        @Nonnull
        public String toString() {
            return "TravelAnchorLocation{"
                    + "worldName=" + worldName
                    + ", uuid=" + uuid
                    + ", anchorName=" + anchorName
                    + ", x=" + x
                    + ", y=" + y
                    + ", z=" + z + "}";

        }
    }
}
