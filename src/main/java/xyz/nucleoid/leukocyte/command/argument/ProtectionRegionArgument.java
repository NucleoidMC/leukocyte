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
import xyz.nucleoid.leukocyte.region.ProtectionRegion;

public final class ProtectionRegionArgument {
    public static final DynamicCommandExceptionType REGION_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            new TranslatableText("Region with key '%s' was not found!", arg)
    );

    public static RequiredArgumentBuilder<ServerCommandSource, String> argument(String name) {
        return CommandManager.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    ServerCommandSource source = context.getSource();
                    Leukocyte leukocyte = Leukocyte.get(source.getMinecraftServer());

                    return CommandSource.suggestMatching(
                            leukocyte.getRegionKeys().stream(),
                            builder
                    );
                });
    }

    public static ProtectionRegion get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, name);

        ServerCommandSource source = context.getSource();
        Leukocyte leukocyte = Leukocyte.get(source.getMinecraftServer());

        ProtectionRegion region = leukocyte.getRegionByKey(key);
        if (region == null) {
            throw REGION_NOT_FOUND.create(key);
        }

        return region;
    }
}
