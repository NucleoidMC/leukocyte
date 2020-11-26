package xyz.nucleoid.leukocyte.authority;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.nucleoid.leukocyte.rule.ProtectionExclusions;
import xyz.nucleoid.leukocyte.rule.ProtectionRuleMap;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;

// TODO: support specific exclusions of a list of players by API?
public final class Authority implements Comparable<Authority> {
    public static final Codec<Authority> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("key").forGetter(authority -> authority.key),
                Codec.INT.fieldOf("level").forGetter(authority -> authority.level),
                AuthorityShapes.CODEC.fieldOf("shapes").forGetter(authority -> authority.shapes),
                ProtectionRuleMap.CODEC.fieldOf("rules").forGetter(authority -> authority.rules),
                ProtectionExclusions.CODEC.fieldOf("exclusions").forGetter(authority -> authority.exclusions)
        ).apply(instance, Authority::new);
    });

    public final String key;
    public final int level;
    public final AuthorityShapes shapes;
    public final ProtectionRuleMap rules;
    public final ProtectionExclusions exclusions;

    public final boolean isTransient;

    Authority(String key, int level, AuthorityShapes shapes, ProtectionRuleMap rules, ProtectionExclusions exclusions, boolean isTransient) {
        this.key = key;
        this.level = level;
        this.shapes = shapes;
        this.rules = rules;
        this.exclusions = exclusions;
        this.isTransient = isTransient;
    }

    Authority(String key, int level, AuthorityShapes shapes, ProtectionRuleMap rules, ProtectionExclusions exclusions) {
        this(key, level, shapes, rules, exclusions, false);
    }

    Authority(String key, int level, AuthorityShapes shapes, boolean isTransient) {
        this(key, level, shapes, new ProtectionRuleMap(), new ProtectionExclusions(), isTransient);
    }

    public static Authority create(String key) {
        return new Authority(key, 0, new AuthorityShapes(), false);
    }

    public static Authority createTransient() {
        String key = RandomStringUtils.randomAlphanumeric(16);
        return new Authority(key, 0, new AuthorityShapes(), true);
    }

    public Authority withLevel(int level) {
        return new Authority(this.key, level, this.shapes, this.rules.copy(), this.exclusions.copy(), this.isTransient);
    }

    public Authority addShape(String name, ProtectionShape shape) {
        AuthorityShapes newShapes = this.shapes.withShape(name, shape);
        return new Authority(this.key, this.level, newShapes, this.rules.copy(), this.exclusions.copy(), this.isTransient);
    }

    public Authority removeShape(String name) {
        AuthorityShapes newShapes = this.shapes.removeShape(name);
        if (this.shapes == newShapes) {
            return this;
        }
        return new Authority(this.key, this.level, newShapes, this.rules.copy(), this.exclusions.copy(), this.isTransient);
    }

    @Override
    public int compareTo(Authority other) {
        int levelCompare = Integer.compare(other.level, this.level);
        if (levelCompare != 0) {
            return levelCompare;
        } else {
            return this.key.compareTo(other.key);
        }
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }
}
