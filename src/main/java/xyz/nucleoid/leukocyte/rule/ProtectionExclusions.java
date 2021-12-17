package xyz.nucleoid.leukocyte.rule;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.leukocyte.roles.PermissionAccessor;
import xyz.nucleoid.leukocyte.roles.RoleAccessor;
import xyz.nucleoid.stimuli.filter.EventFilter;

import java.util.*;

public final class ProtectionExclusions {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<ProtectionExclusions> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.listOf().fieldOf("roles").forGetter(exclusions -> new ArrayList<>(exclusions.roles)),
                Codec.STRING.listOf().optionalFieldOf("permissions", Collections.emptyList()).forGetter(exclusions -> new ArrayList<>(exclusions.permissions)),
                UUID_CODEC.listOf().fieldOf("players").forGetter(exclusions -> new ArrayList<>(exclusions.players)),
                Codec.BOOL.fieldOf("include_operators").forGetter(exclusions -> exclusions.includeOperators)
        ).apply(instance, ProtectionExclusions::new);
    });

    private final Set<String> roles;
    private final Set<UUID> players;

    private boolean includeOperators;
    private Set<String> permissions;

    public ProtectionExclusions() {
        this.roles = new ObjectOpenHashSet<>();
        this.permissions = new ObjectOpenHashSet<>();
        this.players = new ObjectOpenHashSet<>();
    }

    private ProtectionExclusions(Collection<String> roles, Collection<String> permissions, Collection<UUID> players, boolean includeOperators) {
        this.roles = new ObjectOpenHashSet<>(roles);
        this.players = new ObjectOpenHashSet<>(players);
        this.permissions = new ObjectOpenHashSet<>(permissions);
        this.includeOperators = includeOperators;
    }

    public EventFilter applyToFilter(EventFilter filter) {
        return source -> {
            if (filter.accepts(source)) {
                var entity = source.getEntity();
                return !(entity instanceof PlayerEntity player && this.isExcluded(player));
            }
            return false;
        };
    }

    public void includeOperators() {
        this.includeOperators = true;
    }

    public boolean addRole(String role) {
        return this.roles.add(role);
    }

    public boolean removeRole(String role) {
        return this.roles.remove(role);
    }

    public boolean addPermission(String permission) { return this.permissions.add(permission); }

    public boolean removePermission(String permission) { return this.permissions.remove(permission); }

    public boolean addPlayer(GameProfile profile) {
        return this.players.add(profile.getId());
    }

    public boolean removePlayer(GameProfile profile) {
        return this.players.remove(profile.getId());
    }

    public boolean isExcluded(PlayerEntity player) {
        if (!this.includeOperators && player.hasPermissionLevel(4)) {
            return true;
        }

        if (this.players.contains(player.getUuid())) {
            return true;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            for (var excludeRole : this.roles) {
                if (RoleAccessor.INSTANCE.hasRole(serverPlayer, excludeRole)) {
                    return true;
                }
            }

            for (var excludePermission : this.permissions) {
                if (PermissionAccessor.INSTANCE.hasPermission(serverPlayer, excludePermission)) {
                    return true;
                }
            }
        }

        return false;
    }

    public ProtectionExclusions copy() {
        return new ProtectionExclusions(this.roles, this.permissions, this.players, this.includeOperators);
    }
}
