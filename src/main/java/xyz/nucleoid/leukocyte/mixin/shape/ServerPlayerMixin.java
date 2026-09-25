package xyz.nucleoid.leukocyte.mixin.shape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;
import xyz.nucleoid.leukocyte.shape.ShapeBuilder;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.leukocyte.shape.ShapeBuilderInjected;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ShapeBuilderInjected {
    @Unique
    private List<ProtectionShape> shapes;

    @Override
    public void leukocyte$start() {
        this.shapes = new ArrayList<>();
    }

    @Override
    public void leukocyte$add(ProtectionShape shape) {
        this.shapes.add(shape);
    }

    @Override
    public ProtectionShape leukocyte$finish() {
        var shapes = this.shapes.toArray(new ProtectionShape[0]);
        this.shapes = null;
        return ProtectionShape.union(shapes);
    }

    @Override
    public boolean leukocyte$isBuilding() {
        return this.shapes != null;
    }
}
