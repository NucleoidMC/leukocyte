package xyz.nucleoid.leukocyte.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.command.argument.AuthorityArgument;
import xyz.nucleoid.leukocyte.roles.PermissionAccessor;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;
import xyz.nucleoid.leukocyte.shape.ShapeBuilder;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class ShapeCommand {
    private static final SimpleCommandExceptionType NOT_CURRENTLY_BUILDING = new SimpleCommandExceptionType(
            new LiteralMessage("You are not currently building a shape! To start, run /protect shape start")
    );

    private static final SimpleCommandExceptionType ALREADY_BUILDING = new SimpleCommandExceptionType(
            new LiteralMessage("You are already building a shape! To cancel, run /protect shape stop")
    );

    private static final SimpleCommandExceptionType SHAPE_NOT_FOUND = new SimpleCommandExceptionType(
            new LiteralMessage("That shape does not exist!")
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // @formatter:off
        dispatcher.register(
            literal("protect")
                .requires(source -> PermissionAccessor.INSTANCE.hasPermission(source, "leukocyte.commands", PermissionLevel.OWNERS))
                .then(literal("shape")
                    .then(literal("start").executes(ShapeCommand::startShape))
                    .then(literal("stop").executes(ShapeCommand::stopShape))
                    .then(literal("add")
                        .then(literal("universe")
                            .executes(ShapeCommand::addUniversal)
                        )
                        .then(argument( "dimension", DimensionArgument.dimension())
                            .executes(ShapeCommand::addDimension)
                                .then(argument("min", BlockPosArgument.blockPos())
                                .then(argument("max", BlockPosArgument.blockPos())
                                .executes(ShapeCommand::addBox)
                        )))
                            .then(argument("min", BlockPosArgument.blockPos())
                            .then(argument("max", BlockPosArgument.blockPos())
                            .executes(ShapeCommand::addLocalBox)))
                    )
                    .then(literal("finish")
                        .then(argument("name", StringArgumentType.string())
                        .then(literal("to")
                        .then(AuthorityArgument.argument("authority")
                        .executes(ShapeCommand::addShapeToAuthority)
                    ))))
                    .then(literal("remove")
                        .then(argument("name", StringArgumentType.string())
                        .then(literal("from")
                        .then(AuthorityArgument.argument("authority")
                        .executes(ShapeCommand::removeShapeFromAuthority)
                    ))))
                )
        );
        // @formatter:on
    }

    private static int startShape(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayer();

        var builder = ShapeBuilder.start(player);
        if (builder != null) {
            source.sendSuccess(
                    () -> Component.literal("Started building a shape! Use ")
                            .append(Component.literal("/protect shape add").withStyle(ChatFormatting.GRAY))
                            .append(" to add primitives to this shape, and ")
                            .append(Component.literal("/protect shape finish").withStyle(ChatFormatting.GRAY))
                            .append(" to add it to an authority."),
                    false
            );
            return Command.SINGLE_SUCCESS;
        } else {
            throw ALREADY_BUILDING.create();
        }
    }

    private static int stopShape(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayer();

        var builder = ShapeBuilder.from(player);
        if (builder != null) {
            builder.finish();
            source.sendSuccess(() -> Component.literal("Canceled shape building!"), false);
            return Command.SINGLE_SUCCESS;
        } else {
            throw NOT_CURRENTLY_BUILDING.create();
        }
    }

    private static int addBox(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        var min = BlockPosArgument.getBlockPos(context, "min");
        var max = BlockPosArgument.getBlockPos(context, "max");
        return addShape(context.getSource(), ProtectionShape.box(dimension, min, max));
    }

    private static int addLocalBox(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var dimension = context.getSource().getLevel().dimension();
        var min = BlockPosArgument.getBlockPos(context, "min");
        var max = BlockPosArgument.getBlockPos(context, "max");
        return addShape(context.getSource(), ProtectionShape.box(dimension, min, max));
    }

    private static int addDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        return addShape(context.getSource(), ProtectionShape.dimension(dimension));
    }

    private static int addUniversal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return addShape(context.getSource(), ProtectionShape.universe());
    }

    private static int addShape(CommandSourceStack source, ProtectionShape shape) throws CommandSyntaxException {
        var player = source.getPlayer();

        var shapeBuilder = ShapeBuilder.from(player);
        if (shapeBuilder != null) {
            shapeBuilder.add(shape);
            source.sendSuccess(() -> Component.literal("Added ").append(shape.display()).append(" to current shape!"), false);
        } else {
            throw NOT_CURRENTLY_BUILDING.create();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addShapeToAuthority(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayer();

        var name = StringArgumentType.getString(context, "name");
        var authority = AuthorityArgument.get(context, "authority");

        var builder = ShapeBuilder.from(player);
        if (builder != null) {
            var shape = builder.finish();

            var leukocyte = Leukocyte.get(source.getServer());
            leukocyte.replaceAuthority(authority, authority.addShape(name, shape));

            source.sendSuccess(() -> Component.literal("Added shape as '" + name + "' to '" + authority.getKey() + "'!"), true);

            return Command.SINGLE_SUCCESS;
        } else {
            throw NOT_CURRENTLY_BUILDING.create();
        }
    }

    private static int removeShapeFromAuthority(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();

        var authority = AuthorityArgument.get(context, "authority");
        var name = StringArgumentType.getString(context, "name");

        var newAuthority = authority.removeShape(name);
        if (authority == newAuthority) {
            throw SHAPE_NOT_FOUND.create();
        }

        var leukocyte = Leukocyte.get(source.getServer());
        leukocyte.replaceAuthority(authority, newAuthority);

        source.sendSuccess(() -> Component.literal("Removed shape '" + name + "' from '" + authority.getKey() + "'!"), true);

        return Command.SINGLE_SUCCESS;
    }
}
