package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xyz.nucleoid.stimuli.filter.EventFilter;

public final class BoxShape implements ProtectionShape {
    public static final MapCodec<BoxShape> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                Identifier.CODEC.xmap(id -> ResourceKey.create(Registries.DIMENSION, id), ResourceKey::identifier).fieldOf("dimension").forGetter(scope -> scope.dimension),
                BlockPos.CODEC.fieldOf("min").forGetter(scope -> scope.min),
                BlockPos.CODEC.fieldOf("max").forGetter(scope -> scope.max)
        ).apply(instance, BoxShape::new);
    });

    private final ResourceKey<Level> dimension;
    private final BlockPos min;
    private final BlockPos max;

    private final EventFilter eventFilter;

    public BoxShape(ResourceKey<Level> dimension, BlockPos min, BlockPos max) {
        this.dimension = dimension;
        this.min = min;
        this.max = max;

        this.eventFilter = EventFilter.box(dimension, min, max);
    }

    @Override
    public EventFilter asEventFilter() {
        return this.eventFilter;
    }

    @Override
    public MapCodec<? extends ProtectionShape> getCodec() {
        return CODEC;
    }

    @Override
    public MutableComponent display() {
        return Component.literal("[")
                .append(this.displayPos(this.min).withStyle(ChatFormatting.AQUA))
                .append("; ")
                .append(this.displayPos(this.max).withStyle(ChatFormatting.AQUA))
                .append("] in ")
                .append(Component.literal(this.dimension.identifier().toString()).withStyle(ChatFormatting.YELLOW))
                .withStyle(ChatFormatting.GRAY);
    }

    @Override
    public MutableComponent displayShort() {
        return this.display();
    }

    private MutableComponent displayPos(BlockPos pos) {
        return Component.literal("(" + pos.getX() + "; " + pos.getY() + "; " + pos.getZ() + ")");
    }
}
