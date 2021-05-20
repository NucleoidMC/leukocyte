package xyz.nucleoid.leukocyte;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.leukocyte.authority.AuthorityMap;
import xyz.nucleoid.leukocyte.authority.IndexedAuthorityMap;
import xyz.nucleoid.leukocyte.rule.ProtectionRuleMap;
import xyz.nucleoid.leukocyte.rule.enforcer.ProtectionRuleEnforcer;
import xyz.nucleoid.stimuli.event.EventListenerMap;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.ArrayList;
import java.util.List;

public final class Leukocyte extends PersistentState {
    public static final String ID = "leukocyte";

    private static final List<ProtectionRuleEnforcer> RULE_ENFORCERS = new ArrayList<>();

    private final IndexedAuthorityMap authorities = new IndexedAuthorityMap();

    private Leukocyte() {
        super(ID);
    }

    public static Leukocyte get(MinecraftServer server) {
        PersistentStateManager state = server.getOverworld().getPersistentStateManager();
        return state.getOrCreate(Leukocyte::new, ID);
    }

    public static void registerRuleEnforcer(ProtectionRuleEnforcer enforcer) {
        RULE_ENFORCERS.add(enforcer);
    }

    public static EventListenerMap createEventListenersFor(ProtectionRuleMap rules) {
        EventListenerMap listeners = new EventListenerMap();
        for (ProtectionRuleEnforcer enforcer : RULE_ENFORCERS) {
            enforcer.applyTo(rules, listeners);
        }
        return listeners;
    }

    void onWorldLoad(ServerWorld world) {
        this.authorities.addDimension(world.getRegistryKey());
    }

    void onWorldUnload(ServerWorld world) {
        this.authorities.removeDimension(world.getRegistryKey());
    }

    public boolean addAuthority(Authority authority) {
        return this.authorities.add(authority);
    }

    public boolean removeAuthority(Authority authority) {
        return this.removeAuthority(authority.getKey()) != null;
    }

    public Authority removeAuthority(String key) {
        return this.authorities.remove(key);
    }

    public void replaceAuthority(Authority from, Authority to) {
        this.authorities.replace(from, to);
    }

    @Nullable
    public Authority getAuthorityByKey(String key) {
        return this.authorities.byKey(key);
    }

    Iterable<Authority> selectAuthorities(RegistryKey<World> dimension, StimulusEvent<?> event) {
        return this.authorities.select(dimension, event);
    }

    @Override
    public CompoundTag toTag(CompoundTag root) {
        ListTag authorityList = new ListTag();

        for (Authority authority : this.authorities) {
            DataResult<Tag> result = Authority.CODEC.encodeStart(NbtOps.INSTANCE, authority);
            result.result().ifPresent(authorityList::add);
        }

        root.put("authorities", authorityList);

        return root;
    }

    @Override
    public void fromTag(CompoundTag root) {
        this.authorities.clear();

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

    public AuthorityMap getAuthorities() {
        return this.authorities;
    }
}
