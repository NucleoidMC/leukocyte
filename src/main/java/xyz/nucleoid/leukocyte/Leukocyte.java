package xyz.nucleoid.leukocyte;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.leukocyte.authority.AuthorityMap;

import java.util.stream.Stream;

public final class Leukocyte extends PersistentState implements RuleReader {
    public static final String ID = "leukocyte";

    private final AuthorityMap authorities = new AuthorityMap();
    private final Reference2ObjectMap<RegistryKey<World>, AuthorityMap> authoritiesByDimension = new Reference2ObjectOpenHashMap<>();

    private Leukocyte() {
        super(ID);
    }

    public static Leukocyte get(MinecraftServer server) {
        PersistentStateManager state = server.getOverworld().getPersistentStateManager();
        return state.getOrCreate(Leukocyte::new, ID);
    }

    @NotNull
    public static Leukocyte byWorld(ServerWorld world) {
        return get(world.getServer());
    }

    @Nullable
    public static Leukocyte byWorld(World world) {
        return world instanceof ServerWorld ? get(world.getServer()) : null;
    }

    void onWorldLoad(ServerWorld world) {
        RegistryKey<World> dimension = world.getRegistryKey();

        AuthorityMap map = new AuthorityMap();
        for (Authority authority : this.authorities) {
            if (authority.shapes.intersects(dimension)) {
                map.add(authority);
            }
        }

        this.authoritiesByDimension.put(dimension, map);
    }

    void onWorldUnload(ServerWorld world) {
        this.authoritiesByDimension.remove(world.getRegistryKey());
    }

    public boolean addAuthority(Authority authority) {
        if (this.authorities.add(authority)) {
            for (Reference2ObjectMap.Entry<RegistryKey<World>, AuthorityMap> entry : Reference2ObjectMaps.fastIterable(this.authoritiesByDimension)) {
                RegistryKey<World> dimension = entry.getKey();
                if (authority.shapes.intersects(dimension)) {
                    entry.getValue().add(authority);
                }
            }
            return true;
        }
        return false;
    }

    public boolean removeAuthority(Authority authority) {
        return this.removeAuthority(authority.key);
    }

    public boolean removeAuthority(String key) {
        if (this.authorities.remove(key) != null) {
            for (AuthorityMap authorities : this.authoritiesByDimension.values()) {
                authorities.remove(key);
            }
            return true;
        }
        return false;
    }

    public void replaceAuthority(Authority from, Authority to) {
        if (this.authorities.replace(from, to)) {
            for (AuthorityMap dimension : this.authoritiesByDimension.values()) {
                if (!dimension.replace(from, to)) {
                    dimension.add(to);
                }
            }
        }
    }

    @Nullable
    public Authority getAuthorityByKey(String key) {
        return this.authorities.byKey(key);
    }

    private AuthorityMap authoritiesByDimension(RegistryKey<World> dimension) {
        AuthorityMap authoritiesByDimension = this.authoritiesByDimension.get(dimension);
        if (authoritiesByDimension == null) {
            this.authoritiesByDimension.put(dimension, authoritiesByDimension = new AuthorityMap());
        }
        return authoritiesByDimension;
    }

    @Override
    public RuleSample sample(RuleQuery query) {
        RegistryKey<World> dimension = query.getDimension();

        if (dimension != null) {
            return this.sampleInDimension(query, dimension);
        } else if (query.getPos() == null) {
            return this.sampleGlobal(query);
        }

        return RuleSample.EMPTY;
    }

    private RuleSample sampleInDimension(RuleQuery query, RegistryKey<World> dimension) {
        AuthorityMap authoritiesInDimension = this.authoritiesByDimension(dimension);
        if (authoritiesInDimension.isEmpty()) {
            return RuleSample.EMPTY;
        }

        BlockPos pos = query.getPos();
        PlayerEntity source = query.getSource();

        if (pos != null) {
            return new RuleSample.FilterPositionAndExclude(authoritiesInDimension, source, dimension, pos);
        } else {
            return new RuleSample.FilterExclude(authoritiesInDimension, source);
        }
    }

    private RuleSample sampleGlobal(RuleQuery query) {
        return new RuleSample.FilterExclude(this.authorities, query.getSource());
    }

    @Override
    public CompoundTag toTag(CompoundTag root) {
        ListTag authorityList = new ListTag();

        for (Authority authority : this.authorities) {
            if (authority.isTransient) {
                continue;
            }

            DataResult<Tag> result = Authority.CODEC.encodeStart(NbtOps.INSTANCE, authority);
            result.result().ifPresent(authorityList::add);
        }

        root.put("authorities", authorityList);

        return root;
    }

    @Override
    public void fromTag(CompoundTag root) {
        this.authorities.clear();
        this.authoritiesByDimension.clear();

        ListTag authoritiesList = root.getList("authorities", NbtType.COMPOUND);

        for (Tag authorityTag : authoritiesList) {
            Authority.CODEC.decode(NbtOps.INSTANCE, authorityTag)
                    .map(Pair::getFirst)
                    .result()
                    .ifPresent(this::addAuthority);
        }
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public Stream<Authority> authorities() {
        return this.authorities.stream();
    }
}
