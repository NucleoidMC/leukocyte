package xyz.nucleoid.leukocyte.rule;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.leukocyte.authority.Authority;

import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ProtectionRuleMap {
    public static final Codec<ProtectionRuleMap> CODEC = Codec.unboundedMap(ProtectionRule.CODEC, RuleResult.CODEC).xmap(
            map -> {
                var rules = new ProtectionRuleMap();
                rules.map.putAll(map);
                return rules;
            },
            rules -> rules.map
    );

    private final Map<ProtectionRule, RuleResult> map;

    public ProtectionRuleMap() {
        this.map = new Reference2ObjectOpenHashMap<>();
    }

    ProtectionRuleMap(ProtectionRuleMap map) {
        this.map = new Reference2ObjectOpenHashMap<>(map.map);
    }

    private void put(ProtectionRule rule, RuleResult result) {
        if (result != RuleResult.PASS) {
            this.map.put(rule, result);
        } else {
            this.map.remove(rule);
        }
    }

    public ProtectionRuleMap with(ProtectionRule rule, RuleResult result) {
        if (this.test(rule) == result) {
            return this;
        }

        var map = this.copy();
        map.put(rule, result);
        return map;
    }

    @NotNull
    public RuleResult test(ProtectionRule rule) {
        return this.map.getOrDefault(rule, RuleResult.PASS);
    }

    public ProtectionRuleMap copy() {
        return new ProtectionRuleMap(this);
    }

    public MutableComponent clickableDisplay(Authority authority) {
        MutableComponent text = Component.literal("");

        for (var entry : this.map.entrySet()) {
            var rule = entry.getKey();
            var result = entry.getValue();

            text = text.append("  ").append(Component.literal(rule.getKey()).withStyle(ChatFormatting.GRAY))
                    .append(" = ").append(result.clickableDisplay(authority, rule))
                    .append("\n");
        }

        return text;
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }
}
