package xyz.nucleoid.leukocyte.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.leukocyte.command.argument.AuthorityArgument;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;
import xyz.nucleoid.leukocyte.shape.ShapeBuilder;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // @formatter:off
        dispatcher.register(
            literal("protect")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("shape")
                    .then(literal("start").executes(ShapeCommand::startShape))
                    .then(literal("stop").executes(ShapeCommand::stopShape))
                    .then(literal("add")
                        .then(literal("universe")
                            .executes(ShapeCommand::addUniversal)
                        )
                        .then(argument( "dimension", DimensionArgumentType.dimension())
                            .executes(ShapeCommand::addDimension)
                                .then(argument("min", BlockPosArgumentType.blockPos())
                                .then(argument("max", BlockPosArgumentType.blockPos())
                                .executes(ShapeCommand::addBox)
                        )))
                            .then(argument("min", BlockPosArgumentType.blockPos())
                            .then(argument("max", BlockPosArgumentType.blockPos())
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

    private static int startShape(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        ShapeBuilder builder = ShapeBuilder.start(player);
        if (builder != null) {
            source.sendFeedback(
                    new LiteralText("Started building a shape! Use ")
                            .append(new LiteralText("/protect shape add").formatted(Formatting.GRAY))
                            .append(" to add primitives to this shape, and ")
                            .append(new LiteralText("/protect shape finish").formatted(Formatting.GRAY))
                            .append(" to add it to an authority."),
                    false
            );
            return Command.SINGLE_SUCCESS;
        } else {
            throw ALREADY_BUILDING.create();
        }
    }

    private static int stopShape(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        ShapeBuilder builder = ShapeBuilder.from(player);
        if (builder != null) {
            builder.finish();
            source.sendFeedback(new LiteralText("Canceled shape building!"), false);
            return Command.SINGLE_SUCCESS;
        } else {
            throw NOT_CURRENTLY_BUILDING.create();
        }
    }

    private static int addBox(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey();
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");
        return addShape(context.getSource(), ProtectionShape.box(dimension, min, max));
    }

    private static int addLocalBox(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = context.getSource().getWorld().getRegistryKey();
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");
        return addShape(context.getSource(), ProtectionShape.box(dimension, min, max));
    }

    private static int addDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey();
        return addShape(context.getSource(), ProtectionShape.dimension(dimension));
    }

    private static int addUniversal(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return addShape(context.getSource(), ProtectionShape.universe());
    }

    private static int addShape(ServerCommandSource source, ProtectionShape shape) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();

        ShapeBuilder shapeBuilder = ShapeBuilder.from(player);
        if (shapeBuilder != null) {
            shapeBuilder.add(shape);
            source.sendFeedback(new LiteralText("Added ").append(shape.display()).append(" to current shape!"), false);
        } else {
            throw NOT_CURRENTLY_BUILDING.create();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addShapeToAuthority(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        String name = StringArgumentType.getString(context, "name");
        Authority authority = AuthorityArgument.get(context, "authority");

        ShapeBuilder builder = ShapeBuilder.from(player);
        if (builder != null) {
            ProtectionShape shape = builder.finish();

            Leukocyte leukocyte = Leukocyte.get(source.getMinecraftServer());
            leukocyte.replaceAuthority(authority, authority.addShape(name, shape));

            source.sendFeedback(new LiteralText("Added shape as '" + name + "' to '" + authority.getKey() + "'!"), true);

            return Command.SINGLE_SUCCESS;
        } else {
            throw NOT_CURRENTLY_BUILDING.create();
        }
    }

    private static int removeShapeFromAuthority(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        Authority authority = AuthorityArgument.get(context, "authority");
        String name = StringArgumentType.getString(context, "name");

        Authority newAuthority = authority.removeShape(name);
        if (authority == newAuthority) {
            throw SHAPE_NOT_FOUND.create();
        }

        Leukocyte leukocyte = Leukocyte.get(source.getMinecraftServer());
        leukocyte.replaceAuthority(authority, newAuthority);

        source.sendFeedback(new LiteralText("Removed shape '" + name + "' from '" + authority.getKey() + "'!"), true);

        return Command.SINGLE_SUCCESS;
    }
}
