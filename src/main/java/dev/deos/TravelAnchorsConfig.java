package dev.deos;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class TravelAnchorsConfig {
    public static final BuilderCodec<TravelAnchorsConfig> CODEC = BuilderCodec
            .builder(TravelAnchorsConfig.class, TravelAnchorsConfig::new)
            .append(new KeyedCodec<Integer>("Maximum_distance", Codec.INTEGER),
                    (TravelAnchorsConfig, val, extraInfo) -> TravelAnchorsConfig.maximumDistance = val,
                    (TravelAnchorsConfig, extraInfo) -> TravelAnchorsConfig.maximumDistance).add()
            .build();

    public int maximumDistance = 64;
}