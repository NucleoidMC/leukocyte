package xyz.nucleoid.leukocyte.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.arguments.IdentifierArgument;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.leukocyte.command.argument.AuthorityArgument;
import xyz.nucleoid.leukocyte.command.argument.ProtectionRuleArgument;
import xyz.nucleoid.leukocyte.command.argument.RoleArgument;
import xyz.nucleoid.leukocyte.command.argument.RuleResultArgument;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;
import xyz.nucleoid.stimuli.EventSource;

import java.util.ArrayList;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static xyz.nucleoid.leukocyte.Leukocyte.id;

public final class ProtectCommand {
    private static final DynamicCommandExceptionType AUTHORITY_ALREADY_EXISTS = new DynamicCommandExceptionType(id -> {
        return new LiteralMessage("Authority with the id '" + id + "' already exists!");
    });

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // @formatter:off
        dispatcher.register(
            literal("protect")
                .requires(PermissionPredicates.require(id("commands"), PermissionLevel.OWNERS))
                .then(literal("add")
                    .then(argument("authority", StringArgumentType.string())
                    .executes(ProtectCommand::addAuthority)
                        .then(literal("with")
                            .then(literal("universe")
                                .executes(ProtectCommand::addAuthorityWithUniverse)
                            )
                            .then(argument("dimension", DimensionArgument.dimension())
                                .executes(ProtectCommand::addAuthorityWithDimension)
                                    .then(argument("min", BlockPosArgument.blockPos())
                                    .then(argument("max", BlockPosArgument.blockPos())
                                    .executes(ProtectCommand::addAuthorityWithBox)
                            )))
                            .then(argument("min", BlockPosArgument.blockPos())
                            .then(argument("max", BlockPosArgument.blockPos())
                                .executes(ProtectCommand::addAuthorityWithLocalBox)
                            ))
                        )
                ))
                .then(literal("remove")
                    .then(AuthorityArgument.argument("authority")
                    .executes(ProtectCommand::remove)
                ))
                .then(literal("set")
                    .then(literal("rule")
                        .then(AuthorityArgument.argument("authority")
                        .then(ProtectionRuleArgument.argument("rule")
                        .then(RuleResultArgument.argument("result")
                        .executes(ProtectCommand::setRule)
                    ))))
                    .then(literal("level")
                        .then(AuthorityArgument.argument("authority")
                        .then(argument("level", IntegerArgumentType.integer())
                        .executes(ProtectCommand::setLevel)
                    )))
                )
                .then(literal("exclusion")
                    .then(literal("add")
                        .then(AuthorityArgument.argument("authority")
                            .then(literal("player")
                                .then(argument("player", GameProfileArgument.gameProfile())
                                .executes(ProtectCommand::addPlayerExclusion))
                            )

                            .then(literal("role")
                                .then(RoleArgument.argument("role")
                                .executes(ProtectCommand::addRoleExclusion))
                            )

                            .then(literal("permission")
                                .then(argument("permission", IdentifierArgument.id())
                                .executes(ProtectCommand::addPermissionExclusion))
                            )
                    ))
                    .then(literal("remove")
                        .then(AuthorityArgument.argument("authority")
                            .then(literal("player")
                                .then(argument("player", GameProfileArgument.gameProfile())
                                .executes(ProtectCommand::removePlayerExclusion))
                            )

                            .then(literal("role")
                                .then(RoleArgument.argument("role")
                                .executes(ProtectCommand::removeRoleExclusion))
                            )

                            .then(literal("permission")
                                .then(argument("permission", IdentifierArgument.id())
                                .executes(ProtectCommand::removePermissionExclusion))
                            )
                    ))
                )
                .then(literal("display")
                    .then(AuthorityArgument.argument("authority")
                    .executes(ProtectCommand::displayAuthority)
                ))
                .then(literal("list").executes(ProtectCommand::listAuthorities))
                .then(literal("test")
                    .executes(ProtectCommand::testRulesAtSource)
                        .then(argument("entity", EntityArgument.entity())
                        .executes(ProtectCommand::testRulesForEntity)))
        );
        // @formatter:on
    }

    private static int addAuthority(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return addAuthority(context, authority -> authority);
    }

    private static int addAuthorityWithUniverse(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return addAuthority(context, authority -> authority.addShape(authority.getKey(), ProtectionShape.universe()));
    }

    private static int addAuthorityWithBox(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        var min = BlockPosArgument.getBlockPos(context, "min");
        var max = BlockPosArgument.getBlockPos(context, "max");
        return addAuthority(context, authority -> authority.addShape(authority.getKey(), ProtectionShape.box(dimension, min, max)));
    }

    private static int addAuthorityWithLocalBox(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var dimension = context.getSource().getLevel().dimension();
        var min = BlockPosArgument.getBlockPos(context, "min");
        var max = BlockPosArgument.getBlockPos(context, "max");
        return addAuthority(context, authority -> authority.addShape(authority.getKey(), ProtectionShape.box(dimension, min, max)));
    }

    private static int addAuthorityWithDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        return addAuthority(context, authority -> authority.addShape(authority.getKey(), ProtectionShape.dimension(dimension)));
    }

    private static int addAuthority(CommandContext<CommandSourceStack> context, UnaryOperator<Authority> operator) throws CommandSyntaxException {
        var key = StringArgumentType.getString(context, "authority");

        var source = context.getSource();
        var leukocyte = Leukocyte.get(source.getServer());
        var authority = operator.apply(Authority.create(key));

        if (leukocyte.addAuthority(authority)) {
            var shapes = authority.getShapes();
            if (shapes.isEmpty()) {
                source.sendSuccess(() -> Component.literal("Added empty authority as '" + key + "'"), true);
            } else {
                source.sendSuccess(() -> Component.literal("Added authority as '" + key + "' with ").append(shapes.displayShort()), true);
            }

            source.sendSuccess(
                    () -> Component.literal("Run ")
                            .append(Component.literal("/protect shape start").withStyle(ChatFormatting.GRAY))
                            .append(" to include additional shapes in this authority, and ")
                            .append(Component.literal("/protect set rule " + key + " <rule> <allow|deny>").withStyle(ChatFormatting.GRAY))
                            .append(" to set the rules on this authority"),
                    false
            );

            return Command.SINGLE_SUCCESS;
        } else {
            throw AUTHORITY_ALREADY_EXISTS.create(key);
        }
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var authority = AuthorityArgument.get(context, "authority");

        var leukocyte = Leukocyte.get(context.getSource().getServer());
        leukocyte.removeAuthority(authority.getKey());

        context.getSource().sendSuccess(() -> Component.literal("Removed authority " + authority.getKey()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int setRule(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var leukocyte = Leukocyte.get(context.getSource().getServer());
        var authority = AuthorityArgument.get(context, "authority");

        var rule = ProtectionRuleArgument.get(context, "rule");
        var result = RuleResultArgument.get(context, "result");

        var newAuthority = authority.withRule(rule, result);
        leukocyte.replaceAuthority(authority, newAuthority);

        context.getSource().sendSuccess(() -> Component.literal("Set rule " + rule.getKey() + " = " + result.getKey() + " for " + authority.getKey()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int setLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var authority = AuthorityArgument.get(context, "authority");
        int level = IntegerArgumentType.getInteger(context, "level");

        var leukocyte = Leukocyte.get(context.getSource().getServer());

        var newAuthority = authority.withLevel(level);
        leukocyte.replaceAuthority(authority, newAuthority);

        context.getSource().sendSuccess(() -> Component.literal("Changed level of " + authority.getKey() + " from " + authority.getLevel() + " to " + level), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int addPlayerExclusion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var authority = AuthorityArgument.get(context, "authority");
        var players = GameProfileArgument.getGameProfiles(context, "player");

        var exclusions = authority.getExclusions();

        int count = (int) players.stream()
                .filter(exclusions::addPlayer)
                .count();

        context.getSource().sendSuccess(() -> Component.literal("Added " + count + " player exclusions to " + authority.getKey()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int removePlayerExclusion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var authority = AuthorityArgument.get(context, "authority");
        var players = GameProfileArgument.getGameProfiles(context, "player");

        var exclusions = authority.getExclusions();

        int count = (int) players.stream()
                .filter(exclusions::removePlayer)
                .count();

        context.getSource().sendSuccess(() -> Component.literal("Removed " + count + " player exclusions from " + authority.getKey()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int addRoleExclusion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var authority = AuthorityArgument.get(context, "authority");
        var role = RoleArgument.get(context, "role");

        if (authority.getExclusions().addRole(role)) {
            context.getSource().sendSuccess(() -> Component.literal("Added '" + role + "' exclusion to " + authority.getKey()), true);
        } else {
            context.getSource().sendFailure(Component.literal("'" + role + "' is already excluded"));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeRoleExclusion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var authority = AuthorityArgument.get(context, "authority");
        var role = RoleArgument.get(context, "role");

        if (authority.getExclusions().removeRole(role)) {
            context.getSource().sendSuccess(() -> Component.literal("Removed '" + role + "' exclusion from " + authority.getKey()), true);
        } else {
            context.getSource().sendFailure(Component.literal("'" + role + "' is not excluded"));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addPermissionExclusion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var authority = AuthorityArgument.get(context, "authority");
        var permission = IdentifierArgument.getId(context, "permission");

        if (authority.getExclusions().addPermission(permission)) {
            context.getSource().sendSuccess(() -> Component.literal("Added '" + permission + "' exclusion to " + authority.getKey()), true);
        } else {
            context.getSource().sendFailure(Component.literal("'" + permission + "' is already excluded"));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removePermissionExclusion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var authority = AuthorityArgument.get(context, "authority");
        var permission = IdentifierArgument.getId(context, "permission");

        if (authority.getExclusions().removePermission(permission)) {
            context.getSource().sendSuccess(() -> Component.literal("Removed '" + permission + "' exclusion from " + authority.getKey()), true);
        } else {
            context.getSource().sendFailure(Component.literal("'" + permission + "' is not excluded"));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listAuthorities(CommandContext<CommandSourceStack> context) {
        var leukocyte = Leukocyte.get(context.getSource().getServer());

        var authorities = leukocyte.getAuthorities();
        if (authorities.isEmpty()) {
            context.getSource().sendFailure(Component.literal("There are no authorities!"));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendSuccess(() -> {
            MutableComponent text = Component.literal("Listing " + authorities.size() + " registered authorities:\n");
            for (var authority : authorities) {
                text = text.append("  ").append(Component.literal(authority.getKey()).withStyle(ChatFormatting.AQUA)).append("@" + authority.getLevel() + ": ")
                        .append(authority.getShapes().displayShort())
                        .append("\n");
            }

            return text;
        }, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int testRulesAtSource(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return testRules(context, null);
    }

    private static int testRulesForEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var entity = EntityArgument.getEntity(context, "entity");
        return testRules(context, entity);
    }

    private static int testRules(CommandContext<CommandSourceStack> context, Entity entity) throws CommandSyntaxException {
        var source = context.getSource();

        var world = source.getLevel();
        var pos = BlockPos.containing(source.getPosition());

        var leukocyte = Leukocyte.get(source.getServer());

        var authorities = new ArrayList<Authority>();

        try (var eventSource = entity == null ? EventSource.at(world, pos) : EventSource.forEntityAt(entity, pos)) {
            for (var authority : leukocyte.getAuthorities()) {
                if (authority.getEventFilter().accepts(eventSource)) {
                    authorities.add(authority);
                }
            }
        }

        if (authorities.isEmpty()) {
            MutableComponent error = Component.literal("There are no authorities that apply ");

            if (entity != null) {
                error = error.append("to ");
                error = error.append(entity.getDisplayName());
                error = error.append(CommonComponents.SPACE);
            }

            error = error.append("at ");
            error = error.append(ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())));
            error = error.append("!");

            source.sendFailure(error);
            return Command.SINGLE_SUCCESS;
        }

        source.sendSuccess(() -> {
            MutableComponent text = Component.literal("Testing applicable rules ");

            if (entity != null) {
                text = text.append("for ");
                text = text.append(entity.getDisplayName());
                text = text.append(CommonComponents.SPACE);
            }

            text = text.append("at ");
            text = text.append(ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())));
            text = text.append(":\n");

            text = text.append(" from authorities: ");
            for (int i = 0; i < authorities.size(); i++) {
                var tail = i < authorities.size() - 1 ? ", " : "\n\n";

                var authority = authorities.get(i);
                text.append(Component.literal(authority.getKey()).withStyle(ChatFormatting.AQUA)).append(tail);
            }

            boolean empty = true;
            for (var rule : ProtectionRule.REGISTRY) {
                for (var authority : authorities) {
                    var result = authority.getRules().test(rule);
                    if (result != RuleResult.PASS) {
                        text = text.append("  ").append(Component.literal(rule.getKey()).withStyle(ChatFormatting.AQUA))
                                .append(" = ").append(Component.literal(result.getKey()).withStyle(result.getFormatting()))
                                .append(" (" + authority.getKey() + ")\n");
                        empty = false;
                        break;
                    }
                }
            }

            if (empty) {
                text = text.append("  No rules applied!");
            }

            return text;
        }, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int displayAuthority(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var authority = AuthorityArgument.get(context, "authority");

        context.getSource().sendSuccess(() -> {
            MutableComponent text = Component.literal("Information for '" + authority.getKey() + "':\n");
            text = text.append(" Level: ").append(Component.literal(String.valueOf(authority.getLevel())).withStyle(ChatFormatting.AQUA)).append("\n");
            text = text.append(" Shapes:\n").append(authority.getShapes().displayList());

            var rules = authority.getRules();
            if (!rules.isEmpty()) {
                text.append(" Rules:\n").append(rules.clickableDisplay(authority));
            }

            return text;
        }, false);

        return Command.SINGLE_SUCCESS;
    }
}
