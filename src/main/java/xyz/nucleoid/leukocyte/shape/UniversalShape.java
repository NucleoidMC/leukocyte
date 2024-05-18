package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.MapCodec;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.stimuli.filter.EventFilter;

public final class UniversalShape implements ProtectionShape {
    public static final UniversalShape INSTANCE = new UniversalShape();

    public static MapCodec<UniversalShape> CODEC = MapCodec.unit(INSTANCE);

    private UniversalShape() {
    }

    @Override
    public EventFilter asEventFilter() {
        return EventFilter.global();
    }

    @Override
    public MapCodec<? extends ProtectionShape> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        return Text.literal("Universe").formatted(Formatting.YELLOW);
    }

    @Override
    public MutableText displayShort() {
        return this.display();
    }
}
