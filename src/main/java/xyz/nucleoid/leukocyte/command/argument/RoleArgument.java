package xyz.nucleoid.leukocyte.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import xyz.nucleoid.leukocyte.roles.RoleAccessor;

public final class RoleArgument {
    public static RequiredArgumentBuilder<ServerCommandSource, String> argument(String name) {
        return CommandManager.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    return CommandSource.suggestMatching(
                            RoleAccessor.INSTANCE.getAllRoles(),
                            builder
                    );
                });
    }

    public static String get(CommandContext<ServerCommandSource> context, String name) {
        return StringArgumentType.getString(context, name);
    }
}
