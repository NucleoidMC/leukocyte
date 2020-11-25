package xyz.nucleoid.leukocyte;

import com.google.common.collect.AbstractIterator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

import java.util.Collections;
import java.util.Iterator;

public interface RuleSample extends Iterable<Authority> {
    RuleSample EMPTY = Collections::emptyIterator;

    default RuleResult test(ProtectionRule rule) {
        for (Authority authority : this) {
            RuleResult result = authority.rules.test(rule);
            if (result != RuleResult.PASS) {
                return result;
            }
        }
        return RuleResult.PASS;
    }

    default boolean allows(ProtectionRule rule) {
        return this.test(rule) == RuleResult.ALLOW;
    }

    default boolean denies(ProtectionRule rule) {
        return this.test(rule) == RuleResult.DENY;
    }

    abstract class Filtered implements RuleSample {
        final Iterable<Authority> authorities;

        Filtered(Iterable<Authority> authorities) {
            this.authorities = authorities;
        }

        protected abstract boolean test(Authority authority);

        @Override
        public Iterator<Authority> iterator() {
            Iterator<Authority> iterator = this.authorities.iterator();
            return new AbstractIterator<Authority>() {
                @Override
                protected Authority computeNext() {
                    while (iterator.hasNext()) {
                        Authority authority = iterator.next();
                        if (Filtered.this.test(authority)) {
                            return authority;
                        }
                    }
                    return this.endOfData();
                }
            };
        }
    }

    final class FilterExclude extends Filtered {
        private final PlayerEntity source;

        FilterExclude(Iterable<Authority> authorities, PlayerEntity source) {
            super(authorities);
            this.source = source;
        }

        @Override
        protected boolean test(Authority authority) {
            return !authority.exclusions.isExcluded(this.source);
        }

        @Override
        public Iterator<Authority> iterator() {
            return this.source != null ? super.iterator() : this.authorities.iterator();
        }
    }

    final class FilterPositionAndExclude extends Filtered {
        private final PlayerEntity source;
        private final RegistryKey<World> dimension;
        private final BlockPos pos;

        FilterPositionAndExclude(Iterable<Authority> authorities, PlayerEntity source, RegistryKey<World> dimension, BlockPos pos) {
            super(authorities);
            this.source = source;
            this.dimension = dimension;
            this.pos = pos;
        }

        @Override
        protected boolean test(Authority authority) {
            if (!authority.shapes.contains(this.dimension, this.pos)) {
                return false;
            }
            return this.source == null || !authority.exclusions.isExcluded(this.source);
        }
    }
}
