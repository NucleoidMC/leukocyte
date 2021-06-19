package xyz.nucleoid.leukocyte.roles;

import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.api.Role;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.stream.Stream;

public interface RoleAccessor {
    RoleAccessor INSTANCE = FabricLoader.getInstance().isModLoaded("player_roles") ? new PlayerRoles() : new None();

    Stream<String> getAllRoles();

    boolean hasRole(ServerPlayerEntity player, String role);

    final class None implements RoleAccessor {
        None() {
        }

        @Override
        public Stream<String> getAllRoles() {
            return Stream.empty();
        }

        @Override
        public boolean hasRole(ServerPlayerEntity player, String role) {
            return false;
        }
    }

    final class PlayerRoles implements RoleAccessor {
        PlayerRoles() {
        }

        @Override
        public Stream<String> getAllRoles() {
            return PlayerRolesApi.provider().stream().map(Role::getId);
        }

        @Override
        public boolean hasRole(ServerPlayerEntity player, String roleId) {
            var role = PlayerRolesApi.provider().get(roleId);
            if (role != null) {
                return PlayerRolesApi.lookup().byPlayer(player).has(role);
            } else {
                return false;
            }
        }
    }
}
