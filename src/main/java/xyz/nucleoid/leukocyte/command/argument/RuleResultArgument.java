package xyz.nucleoid.leukocyte.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import xyz.nucleoid.leukocyte.rule.RuleResult;

public final class RuleResultArgument {
    public static RequiredArgumentBuilder<CommandSourceStack, String> argument(String name) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    return SharedSuggestionProvider.suggest(
                            RuleResult.keySet().stream(),
                            builder
                    );
                });
    }

    public static RuleResult get(CommandContext<CommandSourceStack> context, String name) {
        var key = StringArgumentType.getString(context, name);
        return RuleResult.byKeyOrPass(key);
    }
}
