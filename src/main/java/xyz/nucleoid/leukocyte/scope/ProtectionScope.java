package xyz.nucleoid.leukocyte.scope;

import com.mojang.serialization.Codec;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.function.Function;

public interface ProtectionScope {
    Codec<ProtectionScope> CODEC = ProtectionScopeRegistry.REGISTRY.dispatchStable(ProtectionScope::getCodec, Function.identity());

    static ProtectionScope global() {
        return GlobalScope.INSTANCE;
    }

    static ProtectionScope dimension(RegistryKey<World> dimension) {
        return new DimensionScope(dimension);
    }

    static ProtectionScope box(RegistryKey<World> dimension, BlockPos a, BlockPos b) {
        BlockPos min = new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
        BlockPos max = new BlockPos(
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ())
        );

        return new BoxScope(dimension, min, max);
    }

    static ProtectionScope union(ProtectionScope... scopes) {
        return new UnionScope(scopes);
    }

    boolean contains(RegistryKey<World> dimension);

    boolean contains(RegistryKey<World> dimension, BlockPos pos);

    Codec<? extends ProtectionScope> getCodec();

    MutableText display();

    default ProtectionScope union(ProtectionScope other) {
        return union(this, other);
    }
}
