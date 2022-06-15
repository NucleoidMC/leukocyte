package xyz.nucleoid.leukocyte.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

public final class ProtectionRuleArgument {
    public static final DynamicCommandExceptionType RULE_DOES_NOT_EXIST = new DynamicCommandExceptionType(arg ->
            Text.translatable("Rule with key '%s' does not exist!", arg)
    );

    public static RequiredArgumentBuilder<ServerCommandSource, String> argument(String name) {
        return CommandManager.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    return CommandSource.suggestMatching(
                            ProtectionRule.keySet().stream(),
                            builder
                    );
                });
    }

    public static ProtectionRule get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var key = StringArgumentType.getString(context, name);
        var rule = ProtectionRule.byKey(key);
        if (rule == null) {
            throw RULE_DOES_NOT_EXIST.create(key);
        }

        return rule;
    }
}
