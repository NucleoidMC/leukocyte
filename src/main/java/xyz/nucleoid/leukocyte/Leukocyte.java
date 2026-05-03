package xyz.nucleoid.leukocyte;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
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

public final class Leukocyte extends SavedData {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("leukocyte", "leukocyte");

    public static final Codec<Leukocyte> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                IndexedAuthorityMap.CODEC.fieldOf("authorities").forGetter(leukocyte -> leukocyte.authorities)
        ).apply(instance, Leukocyte::new);
    });

    private static final List<ProtectionRuleEnforcer> RULE_ENFORCERS = new ArrayList<>();
    private static final SavedDataType<Leukocyte> TYPE = new SavedDataType<>(
            ID, Leukocyte::new,
            CODEC, null
    );

    private final IndexedAuthorityMap authorities;

    private Leukocyte(IndexedAuthorityMap authorities) {
        this.authorities = authorities;
    }

    private Leukocyte() {
        this(new IndexedAuthorityMap());
    }

    public static Leukocyte get(MinecraftServer server) {
        var state = server.getDataStorage();
        return state.computeIfAbsent(TYPE);
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

    void onWorldLoad(ServerLevel world) {
        this.authorities.addDimension(world.dimension());
    }

    void onWorldUnload(ServerLevel world) {
        this.authorities.removeDimension(world.dimension());
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

    Iterable<Authority> selectAuthorities(ResourceKey<Level> dimension, StimulusEvent<?> event) {
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
