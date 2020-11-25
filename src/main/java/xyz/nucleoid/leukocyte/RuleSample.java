package xyz.nucleoid.leukocyte;

import net.minecraft.entity.player.PlayerEntity;
import xyz.nucleoid.leukocyte.region.ProtectionRegion;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

import java.util.Iterator;

public final class RuleSample implements Iterable<ProtectionRegion> {
    private final PlayerEntity source;
    private final Iterable<ProtectionRegion> regions;

    RuleSample(PlayerEntity source, Iterable<ProtectionRegion> regions) {
        this.source = source;
        this.regions = regions;
    }

    @Override
    public Iterator<ProtectionRegion> iterator() {
        return this.regions.iterator();
    }

    public RuleResult test(ProtectionRule rule) {
        PlayerEntity source = this.source;
        if (source != null && source.hasPermissionLevel(4)) {
            return RuleResult.PASS;
        }

        for (ProtectionRegion region : this.regions) {
            RuleResult result = region.rules.test(rule);
            if (result != RuleResult.PASS) {
                return result;
            }
        }

        return RuleResult.PASS;
    }

    public boolean allows(ProtectionRule rule) {
        return this.test(rule) == RuleResult.ALLOW;
    }

    public boolean denies(ProtectionRule rule) {
        return this.test(rule) == RuleResult.DENY;
    }
}
