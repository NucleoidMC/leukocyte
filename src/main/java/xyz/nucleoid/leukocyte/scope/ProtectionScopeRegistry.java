package xyz.nucleoid.leukocyte.scope;

import com.mojang.serialization.Codec;
import xyz.nucleoid.leukocyte.util.TinyRegistry;

public final class ProtectionScopeRegistry {
    static final TinyRegistry<Codec<? extends ProtectionScope>> REGISTRY = TinyRegistry.newStable();

    static {
        register("global", GlobalScope.CODEC);
        register("dimension", DimensionScope.CODEC);
        register("box", BoxScope.CODEC);
    }

    public static <T extends ProtectionScope> void register(String identifier, Codec<T> codec) {
        REGISTRY.register(identifier, codec);
    }
}
