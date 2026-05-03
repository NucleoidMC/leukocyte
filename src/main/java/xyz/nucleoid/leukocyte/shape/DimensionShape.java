package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xyz.nucleoid.stimuli.filter.EventFilter;

public final class DimensionShape implements ProtectionShape {
    public static final MapCodec<DimensionShape> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                Identifier.CODEC.xmap(id -> ResourceKey.create(Registries.DIMENSION, id), ResourceKey::identifier).fieldOf("dimension").forGetter(scope -> scope.dimension)
        ).apply(instance, DimensionShape::new);
    });

    private final ResourceKey<Level> dimension;

    private final EventFilter eventFilter;

    public DimensionShape(ResourceKey<Level> dimension) {
        this.dimension = dimension;

        this.eventFilter = EventFilter.dimension(dimension);
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
        return Component.literal(this.dimension.identifier().toString()).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public MutableComponent displayShort() {
        return this.display();
    }
}
