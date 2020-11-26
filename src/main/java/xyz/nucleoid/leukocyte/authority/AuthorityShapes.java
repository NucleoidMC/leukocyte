package xyz.nucleoid.leukocyte.authority;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;
import xyz.nucleoid.leukocyte.shape.UnionShape;

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

        ProtectionShape[] shapes = new ProtectionShape[entries.length];
        for (int i = 0; i < shapes.length; i++) {
            shapes[i] = entries[i].shape;
        }
        this.combinedShape = new UnionShape(shapes);
    }

    private AuthorityShapes(List<Entry> entries) {
        this(entries.toArray(new Entry[0]));
    }

    public AuthorityShapes withShape(String name, ProtectionShape shape) {
        Entry[] newShapes = Arrays.copyOf(this.entries, this.entries.length + 1);
        newShapes[newShapes.length - 1] = new Entry(name, shape);
        return new AuthorityShapes(newShapes);
    }

    public AuthorityShapes removeShape(String name) {
        int index = this.findIndex(name);
        if (index == -1) {
            return this;
        }

        int writer = 0;

        Entry[] newEntries = new Entry[this.entries.length - 1];
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

    public boolean intersects(RegistryKey<World> dimension) {
        return this.combinedShape.intersects(dimension);
    }

    public boolean contains(RegistryKey<World> dimension, BlockPos pos) {
        return this.combinedShape.contains(dimension, pos);
    }

    public Text displayList() {
        if (this.entries.length == 0) {
            return new LiteralText("Empty\n").formatted(Formatting.YELLOW);
        }

        MutableText text = new LiteralText("");
        for (Entry entry : this.entries) {
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

    public static class Entry {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.STRING.fieldOf("name").forGetter(entry -> entry.name),
                    ProtectionShape.CODEC.fieldOf("shape").forGetter(entry -> entry.shape)
            ).apply(instance, Entry::new);
        });

        public final String name;
        public final ProtectionShape shape;

        public Entry(String name, ProtectionShape shape) {
            this.name = name;
            this.shape = shape;
        }
    }
}
