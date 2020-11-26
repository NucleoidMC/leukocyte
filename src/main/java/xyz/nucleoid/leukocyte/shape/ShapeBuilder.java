package xyz.nucleoid.leukocyte.shape;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface ShapeBuilder {
    @Nullable
    static ShapeBuilder start(ServerPlayerEntity player) {
        ShapeBuilder builder = (ShapeBuilder) player;
        if (!builder.isBuilding()) {
            builder.start();
            return builder;
        } else {
            return null;
        }
    }

    @Nullable
    static ShapeBuilder from(ServerPlayerEntity player) {
        ShapeBuilder builder = (ShapeBuilder) player;
        if (builder.isBuilding()) {
            return builder;
        }
        return null;
    }

    void start();

    void add(ProtectionShape shape);

    ProtectionShape finish();

    boolean isBuilding();
}
