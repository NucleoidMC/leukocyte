package xyz.nucleoid.leukocyte.rule;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.stimuli.event.EventResult;

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
        return Text.literal(this.key).formatted(this.formatting);
    }

    public MutableText clickableDisplay(Authority authority, ProtectionRule rule) {
        if (!this.isDefinitive()) {
            return this.display();
        }

        var command = "/protect set rule " + authority.getKey() + " " + rule.getKey() + " " + this.getOpposite().key;
        var clickEvent = new ClickEvent.SuggestCommand(command);

        return this.display().styled(style -> style.withClickEvent(clickEvent));
    }

    public RuleResult getOpposite() {
        return switch (this) {
            case ALLOW -> RuleResult.DENY;
            case DENY -> RuleResult.ALLOW;
            default -> null;
        };
    }

    public boolean isDefinitive() {
        return this != PASS;
    }

    public RuleResult orElse(RuleResult other) {
        return this.isDefinitive() ? this : other;
    }

    public EventResult asEventResult() {
        return switch (this) {
            case ALLOW -> EventResult.ALLOW;
            case DENY -> EventResult.DENY;
            default -> EventResult.PASS;
        };
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
