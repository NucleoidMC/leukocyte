package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.stimuli.filter.EventFilter;

public final class DimensionShape implements ProtectionShape {
    public static final Codec<DimensionShape> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.xmap(id -> RegistryKey.of(Registry.DIMENSION, id), RegistryKey::getValue).fieldOf("dimension").forGetter(scope -> scope.dimension)
        ).apply(instance, DimensionShape::new);
    });

    private final RegistryKey<World> dimension;

    private final EventFilter eventFilter;

    public DimensionShape(RegistryKey<World> dimension) {
        this.dimension = dimension;

        this.eventFilter = EventFilter.dimension(dimension);
    }

    @Override
    public EventFilter asEventFilter() {
        return this.eventFilter;
    }

    @Override
    public Codec<? extends ProtectionShape> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        return new LiteralText(this.dimension.getValue().toString()).formatted(Formatting.YELLOW);
    }

    @Override
    public MutableText displayShort() {
        return this.display();
    }
}
