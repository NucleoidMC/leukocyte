package xyz.nucleoid.leukocyte.scope;

import com.mojang.serialization.Codec;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public final class GlobalScope implements ProtectionScope {
    public static final GlobalScope INSTANCE = new GlobalScope();

    public static Codec<GlobalScope> CODEC = Codec.unit(INSTANCE);

    private GlobalScope() {
    }

    @Override
    public boolean contains(RegistryKey<World> dimension) {
        return true;
    }

    @Override
    public boolean contains(RegistryKey<World> dimension, BlockPos pos) {
        return true;
    }

    @Override
    public Codec<? extends ProtectionScope> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        return new LiteralText("Global").formatted(Formatting.YELLOW);
    }
}
