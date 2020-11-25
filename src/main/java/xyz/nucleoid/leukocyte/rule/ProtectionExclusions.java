package xyz.nucleoid.leukocyte.rule;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.leukocyte.roles.RoleAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public final class ProtectionExclusions {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<ProtectionExclusions> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.listOf().fieldOf("roles").forGetter(exclusions -> new ArrayList<>(exclusions.roles)),
                UUID_CODEC.listOf().fieldOf("players").forGetter(exclusions -> new ArrayList<>(exclusions.players))
        ).apply(instance, ProtectionExclusions::new);
    });

    private final Set<String> roles;
    private final Set<UUID> players;

    public ProtectionExclusions() {
        this.roles = new ObjectOpenHashSet<>();
        this.players = new ObjectOpenHashSet<>();
    }

    private ProtectionExclusions(Collection<String> roles, Collection<UUID> players) {
        this.roles = new ObjectOpenHashSet<>(roles);
        this.players = new ObjectOpenHashSet<>(players);
    }

    public boolean addRole(String role) {
        return this.roles.add(role);
    }

    public boolean removeRole(String role) {
        return this.roles.remove(role);
    }

    public boolean addPlayer(GameProfile profile) {
        return this.players.add(profile.getId());
    }

    public boolean removePlayer(GameProfile profile) {
        return this.players.remove(profile.getId());
    }

    public boolean isExcluded(PlayerEntity player) {
//        if (player.hasPermissionLevel(4)) {
//            return true;
//        }

        if (this.players.contains(player.getUuid())) {
            return true;
        }

        if (player instanceof ServerPlayerEntity) {
            for (String excludeRole : this.roles) {
                if (RoleAccessor.INSTANCE.hasRole(((ServerPlayerEntity) player), excludeRole)) {
                    return true;
                }
            }
        }

        return false;
    }

    public ProtectionExclusions copy() {
        return new ProtectionExclusions(this.roles, this.players);
    }
}
