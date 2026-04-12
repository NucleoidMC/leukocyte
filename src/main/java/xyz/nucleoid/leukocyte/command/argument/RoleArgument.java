package xyz.nucleoid.leukocyte.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import xyz.nucleoid.leukocyte.roles.RoleAccessor;

public final class RoleArgument {
    public static RequiredArgumentBuilder<CommandSourceStack, String> argument(String name) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    return SharedSuggestionProvider.suggest(
                            RoleAccessor.INSTANCE.getAllRoles(),
                            builder
                    );
                });
    }

    public static String get(CommandContext<CommandSourceStack> context, String name) {
        return StringArgumentType.getString(context, name);
    }
}
