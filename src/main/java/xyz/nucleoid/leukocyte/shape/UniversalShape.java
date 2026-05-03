package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
    public MutableComponent display() {
        return Component.literal("Universe").withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public MutableComponent displayShort() {
        return this.display();
    }
}
