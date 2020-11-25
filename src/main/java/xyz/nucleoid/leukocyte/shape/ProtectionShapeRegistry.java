package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.Codec;
import xyz.nucleoid.leukocyte.util.TinyRegistry;

public final class ProtectionShapeRegistry {
    static final TinyRegistry<Codec<? extends ProtectionShape>> REGISTRY = TinyRegistry.newStable();

    static {
        register("universal", UniversalShape.CODEC);
        register("dimension", DimensionShape.CODEC);
        register("box", BoxShape.CODEC);
        register("union", UnionShape.CODEC);
    }

    public static <T extends ProtectionShape> void register(String identifier, Codec<T> codec) {
        REGISTRY.register(identifier, codec);
    }
}
