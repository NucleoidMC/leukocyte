package xyz.nucleoid.leukocyte.rule;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.leukocyte.authority.Authority;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class ProtectionRuleMap {
    public static final Codec<ProtectionRuleMap> CODEC = Codec.unboundedMap(ProtectionRule.CODEC, RuleResult.CODEC).xmap(
            map -> {
                ProtectionRuleMap rules = new ProtectionRuleMap();
                rules.map.putAll(map);
                return rules;
            },
            rules -> rules.map
    );

    private final Map<ProtectionRule, RuleResult> map = new Reference2ObjectOpenHashMap<>();

    public void put(ProtectionRule rule, RuleResult result) {
        if (result != RuleResult.PASS) {
            this.map.put(rule, result);
        } else {
            this.map.remove(rule);
        }
    }

    @NotNull
    public RuleResult test(ProtectionRule rule) {
        return this.map.getOrDefault(rule, RuleResult.PASS);
    }

    public ProtectionRuleMap copy() {
        ProtectionRuleMap rules = new ProtectionRuleMap();
        rules.map.putAll(this.map);
        return rules;
    }

    public MutableText display() {
        MutableText text = new LiteralText("");

        for (Map.Entry<ProtectionRule, RuleResult> entry : this.map.entrySet()) {
            ProtectionRule rule = entry.getKey();
            RuleResult result = entry.getValue();

            text = text.append("  ").append(new LiteralText(rule.getKey()).formatted(Formatting.GRAY))
                    .append(" = ").append(result.display())
                    .append("\n");
        }

        return text;
    }

    public MutableText clickableDisplay(Authority authority) {
        MutableText text = new LiteralText("");

        for (Map.Entry<ProtectionRule, RuleResult> entry : this.map.entrySet()) {
            ProtectionRule rule = entry.getKey();
            RuleResult result = entry.getValue();

            text = text.append("  ").append(new LiteralText(rule.getKey()).formatted(Formatting.GRAY))
                    .append(" = ").append(result.clickableDisplay(authority, rule))
                    .append("\n");
        }

        return text;
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }
}
