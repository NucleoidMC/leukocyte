package xyz.nucleoid.leukocyte.roles;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.NotNull;

public interface PermissionAccessor {
    PermissionAccessor INSTANCE = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0") ? new FabricPermissionsV0() : new None();

    boolean hasPermission(ServerPlayer player, String permission);

    boolean hasPermission(CommandSourceStack source, String permission, @NotNull PermissionLevel opLevel);

    final class None implements PermissionAccessor {
        None() {
        }

        @Override
        public boolean hasPermission(ServerPlayer player, String permission) {
            return false;
        }

        @Override
        public boolean hasPermission(CommandSourceStack source, String permission, @NotNull PermissionLevel opLevel) {
            return source.permissions().hasPermission(new Permission.HasCommandLevel(opLevel));
        }
    }

    final class FabricPermissionsV0 implements PermissionAccessor {
        FabricPermissionsV0() {
        }

        @Override
        public boolean hasPermission(ServerPlayer player, String permission) {
            return Permissions.check(player, permission);
        }

        @Override
        public boolean hasPermission(CommandSourceStack source, String permission, @NotNull PermissionLevel opLevel) {
            return Permissions.check(source, permission, opLevel);
        }
    }
}
