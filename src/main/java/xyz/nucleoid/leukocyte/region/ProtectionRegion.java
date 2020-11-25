package xyz.nucleoid.leukocyte.region;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.leukocyte.rule.ProtectionRuleMap;
import xyz.nucleoid.leukocyte.scope.ProtectionScope;

// TODO: must support exclusions by role and by API
public final class ProtectionRegion implements Comparable<ProtectionRegion> {
    public static final Codec<ProtectionRegion> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("key").forGetter(region -> region.key),
                Codec.INT.fieldOf("level").forGetter(region -> region.level),
                ProtectionScope.CODEC.fieldOf("scope").forGetter(region -> region.scope),
                ProtectionRuleMap.CODEC.fieldOf("rules").forGetter(region -> region.rules)
        ).apply(instance, ProtectionRegion::new);
    });

    public final String key;
    public final int level;
    public final ProtectionScope scope;
    public final ProtectionRuleMap rules;

    public ProtectionRegion(String key, int level, ProtectionScope scope, ProtectionRuleMap rules) {
        this.key = key;
        this.level = level;
        this.scope = scope;
        this.rules = rules;
    }

    public ProtectionRegion(String key, ProtectionScope scope, int level) {
        this(key, level, scope, new ProtectionRuleMap());
    }

    @Override
    public int compareTo(ProtectionRegion other) {
        return Integer.compare(other.level, this.level);
    }
}
