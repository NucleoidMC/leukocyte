package xyz.nucleoid.leukocyte.roles;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public interface PermissionAccessor {
    PermissionAccessor INSTANCE = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0") ? new FabricPermissionsV0() : new None();

    boolean hasPermission(ServerPlayerEntity player, String permission);

    boolean hasPermission(ServerCommandSource source, String permission, @NotNull PermissionLevel opLevel);

    final class None implements PermissionAccessor {
        None() {
        }

        @Override
        public boolean hasPermission(ServerPlayerEntity player, String permission) {
            return false;
        }

        @Override
        public boolean hasPermission(ServerCommandSource source, String permission, @NotNull PermissionLevel opLevel) {
            return source.getPermissions().hasPermission(new Permission.Level(opLevel));
        }
    }

    final class FabricPermissionsV0 implements PermissionAccessor {
        FabricPermissionsV0() {
        }

        @Override
        public boolean hasPermission(ServerPlayerEntity player, String permission) {
            return Permissions.check(player, permission);
        }

        @Override
        public boolean hasPermission(ServerCommandSource source, String permission, @NotNull PermissionLevel opLevel) {
            return Permissions.check(source, permission, opLevel);
        }
    }
}
