package xyz.nucleoid.leukocyte;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
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

    public static final Codec<Leukocyte> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                IndexedAuthorityMap.CODEC.fieldOf("authorities").forGetter(leukocyte -> leukocyte.authorities)
        ).apply(instance, Leukocyte::new);
    });

    private static final List<ProtectionRuleEnforcer> RULE_ENFORCERS = new ArrayList<>();
    private static final PersistentStateType<Leukocyte> TYPE = new PersistentStateType<>(ID, Leukocyte::new, CODEC, null);

    private final IndexedAuthorityMap authorities;

    private Leukocyte(IndexedAuthorityMap authorities) {
        this.authorities = authorities;
    }

    private Leukocyte() {
        this(new IndexedAuthorityMap());
    }

    public static Leukocyte get(MinecraftServer server) {
        var state = server.getOverworld().getPersistentStateManager();
        return state.getOrCreate(TYPE);
    }

    public static void registerRuleEnforcer(ProtectionRuleEnforcer enforcer) {
        RULE_ENFORCERS.add(enforcer);
    }

    public static EventListenerMap createEventListenersFor(ProtectionRuleMap rules) {
        var listeners = new EventListenerMap();
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
    public boolean isDirty() {
        return true;
    }

    public AuthorityMap getAuthorities() {
        return this.authorities;
    }
}
