package xyz.nucleoid.leukocyte.mixin.shape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;
import xyz.nucleoid.leukocyte.shape.ShapeBuilder;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ShapeBuilder {
    @Unique
    private List<ProtectionShape> shapes;

    @Override
    public void start() {
        this.shapes = new ArrayList<>();
    }

    @Override
    public void add(ProtectionShape shape) {
        this.shapes.add(shape);
    }

    @Override
    public ProtectionShape finish() {
        var shapes = this.shapes.toArray(new ProtectionShape[0]);
        this.shapes = null;
        return ProtectionShape.union(shapes);
    }

    @Override
    public boolean isBuilding() {
        return this.shapes != null;
    }
}
