package xyz.nucleoid.leukocyte.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

public final class ProtectionRuleArgument {
    public static final DynamicCommandExceptionType RULE_DOES_NOT_EXIST = new DynamicCommandExceptionType(arg ->
            Component.translatable("Rule with key '%s' does not exist!", arg)
    );

    public static RequiredArgumentBuilder<CommandSourceStack, String> argument(String name) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    return SharedSuggestionProvider.suggest(
                            ProtectionRule.keySet().stream(),
                            builder
                    );
                });
    }

    public static ProtectionRule get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        var key = StringArgumentType.getString(context, name);
        var rule = ProtectionRule.byKey(key);
        if (rule == null) {
            throw RULE_DOES_NOT_EXIST.create(key);
        }

        return rule;
    }
}
