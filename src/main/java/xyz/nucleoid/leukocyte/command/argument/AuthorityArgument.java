package xyz.nucleoid.leukocyte.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.authority.Authority;

public final class AuthorityArgument {
    public static final DynamicCommandExceptionType AUTHORITY_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            new TranslatableText("Authority with key '%s' was not found!", arg)
    );

    public static RequiredArgumentBuilder<ServerCommandSource, String> argument(String name) {
        return CommandManager.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    ServerCommandSource source = context.getSource();
                    Leukocyte leukocyte = Leukocyte.get(source.getMinecraftServer());

                    return CommandSource.suggestMatching(
                            leukocyte.authorities().filter(authority -> !authority.isTransient).map(authority -> authority.key),
                            builder
                    );
                });
    }

    public static Authority get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, name);

        ServerCommandSource source = context.getSource();
        Leukocyte leukocyte = Leukocyte.get(source.getMinecraftServer());

        Authority authority = leukocyte.getAuthorityByKey(key);
        if (authority == null) {
            throw AUTHORITY_NOT_FOUND.create(key);
        }

        return authority;
    }
}
