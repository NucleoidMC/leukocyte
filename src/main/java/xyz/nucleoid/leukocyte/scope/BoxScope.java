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

public final class BoxScope implements ProtectionScope {
    public static final Codec<BoxScope> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.xmap(id -> RegistryKey.of(Registry.DIMENSION, id), RegistryKey::getValue).fieldOf("dimension").forGetter(scope -> scope.dimension),
                BlockPos.CODEC.fieldOf("min").forGetter(scope -> scope.min),
                BlockPos.CODEC.fieldOf("max").forGetter(scope -> scope.max)
        ).apply(instance, BoxScope::new);
    });

    private final RegistryKey<World> dimension;
    private final BlockPos min;
    private final BlockPos max;

    public BoxScope(RegistryKey<World> dimension, BlockPos min, BlockPos max) {
        this.dimension = dimension;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean contains(RegistryKey<World> dimension) {
        return this.dimension == dimension;
    }

    @Override
    public boolean contains(RegistryKey<World> dimension, BlockPos pos) {
        return this.dimension == dimension
                && pos.getX() >= this.min.getX() && pos.getY() >= this.min.getY() && pos.getZ() >= this.min.getZ()
                && pos.getX() <= this.max.getX() && pos.getY() <= this.max.getY() && pos.getZ() <= this.max.getZ();
    }

    @Override
    public Codec<? extends ProtectionScope> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        return new LiteralText("[")
                .append(this.displayPos(this.min).formatted(Formatting.AQUA))
                .append("; ")
                .append(this.displayPos(this.max).formatted(Formatting.AQUA))
                .append("] in ")
                .append(new LiteralText(this.dimension.getValue().toString()).formatted(Formatting.YELLOW))
                .formatted(Formatting.GRAY);
    }

    private MutableText displayPos(BlockPos pos) {
        return new LiteralText("(" + pos.getX() + "; " + pos.getY() + "; " + pos.getZ() + ")");
    }
}
