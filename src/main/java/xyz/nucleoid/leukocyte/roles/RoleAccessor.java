package xyz.nucleoid.leukocyte.roles;

import dev.gegy.roles.Role;
import dev.gegy.roles.PlayerRolesConfig;
import dev.gegy.roles.api.RoleOwner;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.stream.Stream;

public interface RoleAccessor {
    RoleAccessor INSTANCE = FabricLoader.getInstance().isModLoaded("player_roles") ? new PlayerRoles() : new None();

    Stream<String> getAllRoles();

    Stream<String> getRolesFor(ServerPlayerEntity player);

    boolean hasRole(ServerPlayerEntity player, String role);

    final class None implements RoleAccessor {
        None() {
        }

        @Override
        public Stream<String> getAllRoles() {
            return Stream.empty();
        }

        @Override
        public Stream<String> getRolesFor(ServerPlayerEntity player) {
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
            PlayerRolesConfig roles = PlayerRolesConfig.get();
            return Stream.concat(
                    roles.stream(),
                    Stream.of(roles.everyone())
            ).map(Role::getName);
        }

        @Override
        public Stream<String> getRolesFor(ServerPlayerEntity player) {
            if (player instanceof RoleOwner) {
                return ((RoleOwner) player).getRoles().stream().map(Role::getName);
            }
            return Stream.empty();
        }

        @Override
        public boolean hasRole(ServerPlayerEntity player, String role) {
            if (player instanceof RoleOwner) {
                return ((RoleOwner) player).getRoles().hasRole(role);
            }
            return false;
        }
    }
}
