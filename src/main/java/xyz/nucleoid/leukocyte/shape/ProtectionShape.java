package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import xyz.nucleoid.leukocyte.util.StringRegistry;
import xyz.nucleoid.stimuli.filter.EventFilter;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface ProtectionShape {
    StringRegistry<MapCodec<? extends ProtectionShape>> REGISTRY = new StringRegistry<>();
    Codec<ProtectionShape> CODEC = REGISTRY.dispatchStable(ProtectionShape::getCodec, Function.identity());

    static <T extends ProtectionShape> void register(String identifier, MapCodec<T> codec) {
        REGISTRY.register(identifier, codec);
    }

    static ProtectionShape universe() {
        return UniversalShape.INSTANCE;
    }

    static ProtectionShape dimension(ResourceKey<Level> dimension) {
        return new DimensionShape(dimension);
    }

    static ProtectionShape box(ResourceKey<Level> dimension, BlockPos a, BlockPos b) {
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

    MutableComponent display();

    MutableComponent displayShort();

    default ProtectionShape union(ProtectionShape other) {
        return union(this, other);
    }
}
