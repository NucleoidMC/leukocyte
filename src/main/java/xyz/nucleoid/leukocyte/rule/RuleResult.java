package xyz.nucleoid.leukocyte.rule;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.leukocyte.authority.Authority;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public enum RuleResult {
    PASS("pass", Formatting.YELLOW),
    ALLOW("allow", Formatting.GREEN),
    DENY("deny", Formatting.RED);

    public static final RuleResult[] VALUES = values();
    private static final Map<String, RuleResult> BY_KEY = new Object2ObjectOpenHashMap<>();

    public static final Codec<RuleResult> CODEC = Codec.STRING.xmap(RuleResult::byKeyOrPass, RuleResult::getKey);

    static {
        for (RuleResult result : VALUES) {
            BY_KEY.put(result.key, result);
        }
    }

    private final String key;
    private final Formatting formatting;

    RuleResult(String key, Formatting formatting) {
        this.key = key;
        this.formatting = formatting;
    }

    public String getKey() {
        return this.key;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }

    public MutableText display() {
        return new LiteralText(this.key).formatted(this.formatting);
    }

    public MutableText clickableDisplay(Authority authority, ProtectionRule rule) {
        if (!this.isDefinitive()) {
            return this.display();
        }

        String command = "/protect set rule " + authority.key + " " + rule.getKey() + " " + this.getOpposite().key;
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);

        return this.display().styled(style -> style.withClickEvent(clickEvent));
    }

    public RuleResult getOpposite() {
        switch (this) {
            case ALLOW: return RuleResult.DENY;
            case DENY: return RuleResult.ALLOW;
            case PASS:
            default: return null;
        }
    }

    public boolean isDefinitive() {
        return this != PASS;
    }

    public RuleResult orElse(RuleResult other) {
        return this.isDefinitive() ? this : other;
    }

    @NotNull
    public static RuleResult byKeyOrPass(String key) {
        return BY_KEY.getOrDefault(key, RuleResult.PASS);
    }

    @Nullable
    public static RuleResult byKey(String key) {
        return BY_KEY.get(key);
    }

    public static Set<String> keySet() {
        return BY_KEY.keySet();
    }
}
