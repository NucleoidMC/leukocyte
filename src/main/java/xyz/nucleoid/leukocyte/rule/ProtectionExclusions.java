package xyz.nucleoid.leukocyte.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.leukocyte.roles.RoleAccessor;
import xyz.nucleoid.stimuli.filter.EventFilter;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;

public final class ProtectionExclusions {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<ProtectionExclusions> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.listOf().fieldOf("roles").forGetter(exclusions -> new ArrayList<>(exclusions.roles)),
                Identifier.CODEC.listOf().lenientOptionalFieldOf("permissions", Collections.emptyList()).forGetter(exclusions -> new ArrayList<>(exclusions.permissions)),
                UUID_CODEC.listOf().fieldOf("players").forGetter(exclusions -> new ArrayList<>(exclusions.players)),
                Codec.BOOL.fieldOf("include_operators").forGetter(exclusions -> exclusions.includeOperators)
        ).apply(instance, ProtectionExclusions::new);
    });

    private final Set<String> roles;
    private final Set<UUID> players;

    private boolean includeOperators;
    private Set<Identifier> permissions;

    public ProtectionExclusions() {
        this.roles = new ObjectOpenHashSet<>();
        this.permissions = new ObjectOpenHashSet<>();
        this.players = new ObjectOpenHashSet<>();
    }

    private ProtectionExclusions(Collection<String> roles, Collection<Identifier> permissions, Collection<UUID> players, boolean includeOperators) {
        this.roles = new ObjectOpenHashSet<>(roles);
        this.players = new ObjectOpenHashSet<>(players);
        this.permissions = new ObjectOpenHashSet<>(permissions);
        this.includeOperators = includeOperators;
    }

    public EventFilter applyToFilter(EventFilter filter) {
        return source -> {
            if (filter.accepts(source)) {
                var entity = source.getEntity();
                return !(entity instanceof Player player && this.isExcluded(player));
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

    public boolean addPermission(Identifier permission) { return this.permissions.add(permission); }

    public boolean removePermission(Identifier permission) { return this.permissions.remove(permission); }

    public boolean addPlayer(NameAndId player) {
        return this.players.add(player.id());
    }

    public boolean removePlayer(NameAndId player) {
        return this.players.remove(player.id());
    }

    public boolean isExcluded(Player player) {
        if (!this.includeOperators && player.permissions().hasPermission(Permissions.COMMANDS_OWNER)) {
            return true;
        }

        if (this.players.contains(player.getUUID())) {
            return true;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            for (var excludeRole : this.roles) {
                if (RoleAccessor.INSTANCE.hasRole(serverPlayer, excludeRole)) {
                    return true;
                }
            }

            for (var excludePermission : this.permissions) {
                if (serverPlayer.checkPermission(excludePermission, false)) {
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
