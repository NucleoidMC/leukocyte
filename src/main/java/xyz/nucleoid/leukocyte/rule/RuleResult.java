package xyz.nucleoid.leukocyte.rule;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.stimuli.event.EventResult;

import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum RuleResult {
    PASS("pass", ChatFormatting.YELLOW),
    ALLOW("allow", ChatFormatting.GREEN),
    DENY("deny", ChatFormatting.RED);

    public static final RuleResult[] VALUES = values();
    private static final Map<String, RuleResult> BY_KEY = new Object2ObjectOpenHashMap<>();

    public static final Codec<RuleResult> CODEC = Codec.STRING.xmap(RuleResult::byKeyOrPass, RuleResult::getKey);

    static {
        for (RuleResult result : VALUES) {
            BY_KEY.put(result.key, result);
        }
    }

    private final String key;
    private final ChatFormatting formatting;

    RuleResult(String key, ChatFormatting formatting) {
        this.key = key;
        this.formatting = formatting;
    }

    public String getKey() {
        return this.key;
    }

    public ChatFormatting getFormatting() {
        return this.formatting;
    }

    public MutableComponent display() {
        return Component.literal(this.key).withStyle(this.formatting);
    }

    public MutableComponent clickableDisplay(Authority authority, ProtectionRule rule) {
        if (!this.isDefinitive()) {
            return this.display();
        }

        var command = "/protect set rule " + authority.getKey() + " " + rule.getKey() + " " + this.getOpposite().key;
        var clickEvent = new ClickEvent.SuggestCommand(command);

        return this.display().withStyle(style -> style.withClickEvent(clickEvent));
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
