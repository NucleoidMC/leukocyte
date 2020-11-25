package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.Codec;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.function.Function;

public interface ProtectionShape {
    Codec<ProtectionShape> CODEC = ProtectionShapeRegistry.REGISTRY.dispatchStable(ProtectionShape::getCodec, Function.identity());

    static ProtectionShape global() {
        return UniversalShape.INSTANCE;
    }

    static ProtectionShape dimension(RegistryKey<World> dimension) {
        return new DimensionShape(dimension);
    }

    static ProtectionShape box(RegistryKey<World> dimension, BlockPos a, BlockPos b) {
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

        return new BoxShape(dimension, min, max);
    }

    static ProtectionShape union(ProtectionShape... scopes) {
        return new UnionShape(scopes);
    }

    boolean intersects(RegistryKey<World> dimension);

    boolean contains(RegistryKey<World> dimension, BlockPos pos);

    Codec<? extends ProtectionShape> getCodec();

    MutableText display();

    default ProtectionShape union(ProtectionShape other) {
        return union(this, other);
    }
}
