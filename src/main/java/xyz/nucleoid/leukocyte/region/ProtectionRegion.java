package xyz.nucleoid.leukocyte.region;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.leukocyte.rule.ProtectionExclusions;
import xyz.nucleoid.leukocyte.rule.ProtectionRuleMap;
import xyz.nucleoid.leukocyte.scope.ProtectionScope;

// TODO: support specific exclusions of a list of players by API?
// TODO: keyed regions isn't useful for the purpose of minigames
// TODO: compose a region out of multiple shapes?
public final class ProtectionRegion implements Comparable<ProtectionRegion> {
    public static final Codec<ProtectionRegion> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("key").forGetter(region -> region.key),
                Codec.INT.fieldOf("level").forGetter(region -> region.level),
                ProtectionScope.CODEC.fieldOf("scope").forGetter(region -> region.scope),
                ProtectionRuleMap.CODEC.fieldOf("rules").forGetter(region -> region.rules),
                ProtectionExclusions.CODEC.fieldOf("exclusions").forGetter(region -> region.exclusions)
        ).apply(instance, ProtectionRegion::new);
    });

    public final String key;
    public final int level;
    public final ProtectionScope scope;
    public final ProtectionRuleMap rules;
    public final ProtectionExclusions exclusions;

    public ProtectionRegion(String key, int level, ProtectionScope scope, ProtectionRuleMap rules, ProtectionExclusions exclusions) {
        this.key = key;
        this.level = level;
        this.scope = scope;
        this.rules = rules;
        this.exclusions = exclusions;
    }

    public ProtectionRegion(String key, int level, ProtectionScope scope) {
        this(key, level, scope, new ProtectionRuleMap(), new ProtectionExclusions());
    }

    @Override
    public int compareTo(ProtectionRegion other) {
        return Integer.compare(other.level, this.level);
    }
}
