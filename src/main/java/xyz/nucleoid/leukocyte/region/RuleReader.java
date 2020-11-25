package xyz.nucleoid.leukocyte.region;

import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.RuleSample;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

public interface RuleReader {
    RuleSample sample(RuleQuery query);

    default RuleResult test(RuleQuery query, ProtectionRule rule) {
        return this.sample(query).test(rule);
    }

    default boolean allows(RuleQuery query, ProtectionRule rule) {
        return this.test(query, rule) == RuleResult.ALLOW;
    }

    default boolean denies(RuleQuery query, ProtectionRule rule) {
        return this.test(query, rule) == RuleResult.DENY;
    }
}
