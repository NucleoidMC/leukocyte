package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.util.StringRegistry;
import xyz.nucleoid.stimuli.filter.EventFilter;

import java.util.function.Function;

public interface ProtectionShape {
    StringRegistry<MapCodec<? extends ProtectionShape>> REGISTRY = new StringRegistry<>();
    Codec<ProtectionShape> CODEC = REGISTRY.dispatchStable(ProtectionShape::getCodec, Function.identity());

    static <T extends ProtectionShape> void register(String identifier, MapCodec<T> codec) {
        REGISTRY.register(identifier, codec);
    }

    static ProtectionShape universe() {
        return UniversalShape.INSTANCE;
    }

    static ProtectionShape dimension(RegistryKey<World> dimension) {
        return new DimensionShape(dimension);
    }

    static ProtectionShape box(RegistryKey<World> dimension, BlockPos a, BlockPos b) {
        var min = new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
        var max = new BlockPos(
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ())
        );

        return new BoxShape(dimension, min, max);
    }

    static ProtectionShape union(ProtectionShape... scopes) {
        return new UnionShape(scopes);
    }

    EventFilter asEventFilter();

    MapCodec<? extends ProtectionShape> getCodec();

    MutableText display();

    MutableText displayShort();

    default ProtectionShape union(ProtectionShape other) {
        return union(this, other);
    }
}
