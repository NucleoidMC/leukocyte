package xyz.nucleoid.leukocyte;

import com.google.common.collect.AbstractIterator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.region.ProtectionRegion;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

import java.util.Collections;
import java.util.Iterator;

public interface RuleSample extends Iterable<ProtectionRegion> {
    RuleSample EMPTY = Collections::emptyIterator;

    default RuleResult test(ProtectionRule rule) {
        for (ProtectionRegion region : this) {
            RuleResult result = region.rules.test(rule);
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
        final Iterable<ProtectionRegion> regions;

        Filtered(Iterable<ProtectionRegion> regions) {
            this.regions = regions;
        }

        protected abstract boolean test(ProtectionRegion region);

        @Override
        public Iterator<ProtectionRegion> iterator() {
            Iterator<ProtectionRegion> iterator = this.regions.iterator();
            return new AbstractIterator<ProtectionRegion>() {
                @Override
                protected ProtectionRegion computeNext() {
                    while (iterator.hasNext()) {
                        ProtectionRegion region = iterator.next();
                        if (Filtered.this.test(region)) {
                            return region;
                        }
                    }
                    return this.endOfData();
                }
            };
        }
    }

    final class FilterExclude extends Filtered {
        private final PlayerEntity source;

        FilterExclude(Iterable<ProtectionRegion> regions, PlayerEntity source) {
            super(regions);
            this.source = source;
        }

        @Override
        protected boolean test(ProtectionRegion region) {
            return !region.exclusions.isExcluded(this.source);
        }

        @Override
        public Iterator<ProtectionRegion> iterator() {
            return this.source != null ? super.iterator() : this.regions.iterator();
        }
    }

    final class FilterPositionAndExclude extends Filtered {
        private final PlayerEntity source;
        private final RegistryKey<World> dimension;
        private final BlockPos pos;

        FilterPositionAndExclude(Iterable<ProtectionRegion> regions, PlayerEntity source, RegistryKey<World> dimension, BlockPos pos) {
            super(regions);
            this.source = source;
            this.dimension = dimension;
            this.pos = pos;
        }

        @Override
        protected boolean test(ProtectionRegion region) {
            if (!region.scope.contains(this.dimension, this.pos)) {
                return false;
            }
            return this.source == null || !region.exclusions.isExcluded(this.source);
        }
    }
}
