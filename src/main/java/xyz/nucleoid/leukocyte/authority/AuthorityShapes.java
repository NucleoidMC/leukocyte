package xyz.nucleoid.leukocyte.authority;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;
import xyz.nucleoid.leukocyte.shape.UnionShape;

import java.util.Arrays;
import java.util.List;

public final class AuthorityShapes {
    public static final Codec<AuthorityShapes> CODEC = ProtectionShape.CODEC.listOf().xmap(
            AuthorityShapes::new,
            shapes -> Arrays.asList(shapes.shapes)
    );

    public final ProtectionShape[] shapes;
    private final UnionShape combinedShape;

    public AuthorityShapes(ProtectionShape... shapes) {
        this.shapes = shapes;
        this.combinedShape = new UnionShape(shapes);
    }

    private AuthorityShapes(List<ProtectionShape> shapes) {
        this(shapes.toArray(new ProtectionShape[0]));
    }

    public AuthorityShapes withShape(ProtectionShape shape) {
        ProtectionShape[] newShapes = Arrays.copyOf(this.shapes, this.shapes.length + 1);
        newShapes[newShapes.length - 1] = shape;
        return new AuthorityShapes(newShapes);
    }

    public boolean intersects(RegistryKey<World> dimension) {
        return this.combinedShape.intersects(dimension);
    }

    public boolean contains(RegistryKey<World> dimension, BlockPos pos) {
        return this.combinedShape.contains(dimension, pos);
    }

    public Text display() {
        return this.combinedShape.display();
    }
}
