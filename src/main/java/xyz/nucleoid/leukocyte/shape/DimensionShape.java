package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import xyz.nucleoid.stimuli.filter.EventFilter;

public final class DimensionShape implements ProtectionShape {
    public static final MapCodec<DimensionShape> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                Identifier.CODEC.xmap(id -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue).fieldOf("dimension").forGetter(scope -> scope.dimension)
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
    public MapCodec<? extends ProtectionShape> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        return Text.literal(this.dimension.getValue().toString()).formatted(Formatting.YELLOW);
    }

    @Override
    public MutableText displayShort() {
        return this.display();
    }
}
