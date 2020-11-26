package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.Codec;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public final class UniversalShape implements ProtectionShape {
    public static final UniversalShape INSTANCE = new UniversalShape();

    public static Codec<UniversalShape> CODEC = Codec.unit(INSTANCE);

    private UniversalShape() {
    }

    @Override
    public boolean intersects(RegistryKey<World> dimension) {
        return true;
    }

    @Override
    public boolean contains(RegistryKey<World> dimension, BlockPos pos) {
        return true;
    }

    @Override
    public Codec<? extends ProtectionShape> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        return new LiteralText("Universe").formatted(Formatting.YELLOW);
    }

    @Override
    public MutableText displayShort() {
        return this.display();
    }
}
