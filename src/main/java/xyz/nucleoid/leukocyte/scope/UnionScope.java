package xyz.nucleoid.leukocyte.scope;

import com.mojang.serialization.Codec;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public final class UnionScope implements ProtectionScope {
    public static final Codec<UnionScope> CODEC = ProtectionScope.CODEC.listOf().xmap(
            UnionScope::new,
            union -> Arrays.asList(union.scopes)
    );

    private final ProtectionScope[] scopes;

    public UnionScope(ProtectionScope... scopes) {
        this.scopes = scopes;
    }

    private UnionScope(List<ProtectionScope> scopes) {
        this(scopes.toArray(new ProtectionScope[0]));
    }

    @Override
    public boolean contains(RegistryKey<World> dimension) {
        for (ProtectionScope scope : this.scopes) {
            if (scope.contains(dimension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(RegistryKey<World> dimension, BlockPos pos) {
        for (ProtectionScope scope : this.scopes) {
            if (scope.contains(dimension, pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Codec<? extends ProtectionScope> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        MutableText text = new LiteralText("(");
        for (int i = 0; i < this.scopes.length; i++) {
            text = text.append(this.scopes[i].display());
            if (i <= this.scopes.length - 1) {
                text = text.append("U");
            }
        }
        return text.append(")");
    }

    @Override
    public ProtectionScope union(ProtectionScope other) {
        ProtectionScope[] scopes = new ProtectionScope[this.scopes.length + 1];
        System.arraycopy(this.scopes, 0, scopes, 0, this.scopes.length);
        scopes[scopes.length - 1] = other;
        return new UnionScope(scopes);
    }
}
