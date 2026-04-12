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
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.authority.Authority;

public final class AuthorityArgument {
    public static final DynamicCommandExceptionType AUTHORITY_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            Component.translatable("Authority with key '%s' was not found!", arg)
    );

    public static RequiredArgumentBuilder<CommandSourceStack, String> argument(String name) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    var source = context.getSource();
                    var leukocyte = Leukocyte.get(source.getServer());

                    return SharedSuggestionProvider.suggest(
                            leukocyte.getAuthorities().stream().map(Authority::getKey),
                            builder
                    );
                });
    }

    public static Authority get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        var key = StringArgumentType.getString(context, name);

        var source = context.getSource();
        var leukocyte = Leukocyte.get(source.getServer());

        var authority = leukocyte.getAuthorityByKey(key);
        if (authority == null) {
            throw AUTHORITY_NOT_FOUND.create(key);
        }

        return authority;
    }
}
