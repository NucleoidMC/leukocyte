package xyz.nucleoid.leukocyte.shape;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import xyz.nucleoid.stimuli.filter.EventFilter;

import java.util.Arrays;
import java.util.List;

public final class UnionShape implements ProtectionShape {
    public static final Codec<UnionShape> CODEC = ProtectionShape.CODEC.listOf().xmap(
            UnionShape::new,
            union -> Arrays.asList(union.scopes)
    );

    private final ProtectionShape[] scopes;
    private final EventFilter filter;

    public UnionShape(ProtectionShape... scopes) {
        this.scopes = scopes;

        var filters = new EventFilter[scopes.length];
        for (int i = 0; i < scopes.length; i++) {
            filters[i] = scopes[i].asEventFilter();
        }

        this.filter = EventFilter.anyOf(filters);
    }

    private UnionShape(List<ProtectionShape> scopes) {
        this(scopes.toArray(new ProtectionShape[0]));
    }

    @Override
    public EventFilter asEventFilter() {
        return this.filter;
    }

    @Override
    public Codec<? extends ProtectionShape> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        if (this.scopes.length == 1) {
            return this.scopes[0].display();
        } else if (this.scopes.length == 0) {
            return Text.literal("()");
        }

        MutableText text = Text.literal("(");
        for (int i = 0; i < this.scopes.length; i++) {
            text = text.append(this.scopes[i].display());
            if (i < this.scopes.length - 1) {
                text = text.append("U");
            }
        }
        return text.append(")");
    }

    @Override
    public MutableText displayShort() {
        if (this.scopes.length == 1) {
            return this.scopes[0].display();
        } else if (this.scopes.length == 0) {
            return Text.literal("()");
        }
        return Text.literal(this.scopes.length + " combined shapes");
    }

    @Override
    public ProtectionShape union(ProtectionShape other) {
        var scopes = new ProtectionShape[this.scopes.length + 1];
        System.arraycopy(this.scopes, 0, scopes, 0, this.scopes.length);
        scopes[scopes.length - 1] = other;
        return new UnionShape(scopes);
    }
}
