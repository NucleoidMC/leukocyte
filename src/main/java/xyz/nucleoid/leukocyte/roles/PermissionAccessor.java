package xyz.nucleoid.leukocyte.roles;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PermissionAccessor {
    PermissionAccessor INSTANCE = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0") ? new FabricPermissionsV0() : new None();

    boolean hasPermission(ServerPlayerEntity player, String permission);

    final class None implements PermissionAccessor {
        None() {
        }

        @Override
        public boolean hasPermission(ServerPlayerEntity player, String permission) {
            return false;
        }
    }

    final class FabricPermissionsV0 implements PermissionAccessor {
        FabricPermissionsV0() {
        }

        @Override
        public boolean hasPermission(ServerPlayerEntity player, String permission) {
            return Permissions.check(player, permission);
        }
    }
}
