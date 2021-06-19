package xyz.nucleoid.leukocyte.authority;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;
import xyz.nucleoid.leukocyte.shape.UnionShape;
import xyz.nucleoid.stimuli.filter.EventFilter;

import java.util.Arrays;
import java.util.List;

public final class AuthorityShapes {
    public static final Codec<AuthorityShapes> CODEC = Entry.CODEC.listOf().xmap(
            AuthorityShapes::new,
            shapes -> Arrays.asList(shapes.entries)
    );

    public final Entry[] entries;
    private final UnionShape combinedShape;

    public AuthorityShapes(Entry... entries) {
        this.entries = entries;

        var shapes = new ProtectionShape[entries.length];
        for (int i = 0; i < shapes.length; i++) {
            shapes[i] = entries[i].shape;
        }
        this.combinedShape = new UnionShape(shapes);
    }

    private AuthorityShapes(List<Entry> entries) {
        this(entries.toArray(new Entry[0]));
    }

    public AuthorityShapes withShape(String name, ProtectionShape shape) {
        var newShapes = Arrays.copyOf(this.entries, this.entries.length + 1);
        newShapes[newShapes.length - 1] = new Entry(name, shape);
        return new AuthorityShapes(newShapes);
    }

    public AuthorityShapes removeShape(String name) {
        int index = this.findIndex(name);
        if (index == -1) {
            return this;
        }

        int writer = 0;

        var newEntries = new Entry[this.entries.length - 1];
        for (int i = 0; i < this.entries.length; i++) {
            if (i != index) {
                newEntries[writer++] = this.entries[i];
            }
        }

        return new AuthorityShapes(newEntries);
    }

    private int findIndex(String name) {
        int index = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i].name.equals(name)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public EventFilter asEventFilter() {
        return this.combinedShape.asEventFilter();
    }

    public Text displayList() {
        if (this.entries.length == 0) {
            return new LiteralText("Empty\n").formatted(Formatting.YELLOW);
        }

        MutableText text = new LiteralText("");
        for (var entry : this.entries) {
            text = text.append(new LiteralText("  " + entry.name).formatted(Formatting.AQUA))
                    .append(": ")
                    .append(entry.shape.displayShort())
                    .append("\n");
        }

        return text;
    }

    public Text displayShort() {
        return this.combinedShape.displayShort();
    }

    public boolean isEmpty() {
        return this.entries.length == 0;
    }

    public record Entry(String name, ProtectionShape shape) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.STRING.fieldOf("name").forGetter(entry -> entry.name),
                    ProtectionShape.CODEC.fieldOf("shape").forGetter(entry -> entry.shape)
            ).apply(instance, Entry::new);
        });
    }
}
