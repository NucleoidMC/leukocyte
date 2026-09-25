package xyz.nucleoid.leukocyte.shape;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface ShapeBuilder {
    @Nullable
    static ShapeBuilder start(ServerPlayer player) {
        var builder = ((ShapeBuilderInjected) player).leukocyte$getAsBuilder();
        if (!builder.isBuilding()) {
            builder.start();
            return builder;
        } else {
            return null;
        }
    }

    @Nullable
    static ShapeBuilder from(ServerPlayer player) {
        var builder = ((ShapeBuilderInjected) player).leukocyte$getAsBuilder();
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
