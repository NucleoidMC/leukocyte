package xyz.nucleoid.leukocyte.rule;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.leukocyte.util.TinyRegistry;

import java.util.Set;

public final class ProtectionRule {
    public static final TinyRegistry<ProtectionRule> REGISTRY = TinyRegistry.newStable();

    public static final Codec<ProtectionRule> CODEC = REGISTRY;

    public static final ProtectionRule BREAK = register("break");
    public static final ProtectionRule PLACE = register("place");
    public static final ProtectionRule BLOCK_DROPS = register("block_drops");

    public static final ProtectionRule INTERACT_BLOCKS = register("interact_blocks");
    public static final ProtectionRule INTERACT_ENTITIES = register("interact_entities");
    public static final ProtectionRule INTERACT = register("interact");

    public static final ProtectionRule ATTACK = register("attack");
    public static final ProtectionRule PVP = register("pvp");

    public static final ProtectionRule PORTALS = register("portals");
    public static final ProtectionRule CRAFTING = register("crafting");
    public static final ProtectionRule HUNGER = register("hunger");
    public static final ProtectionRule FALL_DAMAGE = register("fall_damage");
    public static final ProtectionRule THROW_ITEMS = register("throw_items");

    public static final ProtectionRule UNSTABLE_TNT = register("unstable_tnt");
    public static final ProtectionRule IGNITE_TNT = register("ignite_tnt");
    public static final ProtectionRule SPAWN_WITHER = register("spawn_wither");
    public static final ProtectionRule FIRE_TICK = register("fire_tick");

    public static final ProtectionRule FIRE_TICK = register("fire_tick");
    public static final ProtectionRule FLUID_FLOW = register("fluid_flow");

    private final String key;

    ProtectionRule(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return this.key;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Nullable
    public static ProtectionRule byKey(String key) {
        return REGISTRY.get(key);
    }

    public static Set<String> keySet() {
        return REGISTRY.keySet();
    }

    public static ProtectionRule register(String key) {
        ProtectionRule rule = new ProtectionRule(key);
        REGISTRY.register(key, rule);
        return rule;
    }
}
