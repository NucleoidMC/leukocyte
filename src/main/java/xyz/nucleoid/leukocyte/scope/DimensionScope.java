package xyz.nucleoid.leukocyte.scope;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public final class DimensionScope implements ProtectionScope {
    public static final Codec<DimensionScope> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.xmap(id -> RegistryKey.of(Registry.DIMENSION, id), RegistryKey::getValue).fieldOf("dimension").forGetter(scope -> scope.dimension)
        ).apply(instance, DimensionScope::new);
    });

    private final RegistryKey<World> dimension;

    public DimensionScope(RegistryKey<World> dimension) {
        this.dimension = dimension;
    }

    @Override
    public boolean contains(RegistryKey<World> dimension) {
        return this.dimension == dimension;
    }

    @Override
    public boolean contains(RegistryKey<World> dimension, BlockPos pos) {
        return this.contains(dimension);
    }

    @Override
    public Codec<? extends ProtectionScope> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        return new LiteralText(this.dimension.getValue().toString()).formatted(Formatting.YELLOW);
    }
}
