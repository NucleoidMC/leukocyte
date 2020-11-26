package xyz.nucleoid.leukocyte.command;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.RuleSample;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.leukocyte.command.argument.AuthorityArgument;
import xyz.nucleoid.leukocyte.command.argument.ProtectionRuleArgument;
import xyz.nucleoid.leukocyte.command.argument.RoleArgument;
import xyz.nucleoid.leukocyte.command.argument.RuleResultArgument;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;
import xyz.nucleoid.leukocyte.shape.ProtectionShape;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ProtectCommand {
    private static final DynamicCommandExceptionType AUTHORITY_ALREADY_EXISTS = new DynamicCommandExceptionType(id -> {
        return new LiteralMessage("Authority with the id '" + id + "' already exists!");
    });

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // @formatter:off
        dispatcher.register(
            literal("protect")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("add")
                    .then(argument("authority", StringArgumentType.string())
                    .executes(ProtectCommand::addAuthority)
                        .then(literal("with")
                            .then(literal("universe")
                                .executes(ProtectCommand::addAuthorityWithUniverse)
                            )
                            .then(argument("dimension", DimensionArgumentType.dimension())
                                .executes(ProtectCommand::addAuthorityWithDimension)
                                    .then(argument("min", BlockPosArgumentType.blockPos())
                                    .then(argument("max", BlockPosArgumentType.blockPos())
                                    .executes(ProtectCommand::addAuthorityWithBox)
                            )))
                            .then(argument("min", BlockPosArgumentType.blockPos())
                            .then(argument("max", BlockPosArgumentType.blockPos())
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
                            .then(argument("player", GameProfileArgumentType.gameProfile())
                            .executes(ProtectCommand::addPlayerExclusion))

                            .then(RoleArgument.argument("role")
                            .executes(ProtectCommand::addRoleExclusion))
                    ))
                    .then(literal("remove")
                        .then(AuthorityArgument.argument("authority")
                            .then(argument("player", GameProfileArgumentType.gameProfile())
                            .executes(ProtectCommand::removePlayerExclusion))

                            .then(RoleArgument.argument("role")
                            .executes(ProtectCommand::removeRoleExclusion))
                    ))
                )
                .then(literal("display")
                    .then(AuthorityArgument.argument("authority")
                    .executes(ProtectCommand::displayAuthority)
                ))
                .then(literal("list").executes(ProtectCommand::listAuthorities))
                .then(literal("test").executes(ProtectCommand::testRulesHere))
        );
        // @formatter:on
    }

    private static int addAuthority(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return addAuthority(context, authority -> authority);
    }

    private static int addAuthorityWithUniverse(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return addAuthority(context, authority -> authority.addShape(authority.key, ProtectionShape.universe()));
    }

    private static int addAuthorityWithBox(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey();
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");
        return addAuthority(context, authority -> authority.addShape(authority.key, ProtectionShape.box(dimension, min, max)));
    }

    private static int addAuthorityWithLocalBox(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = context.getSource().getWorld().getRegistryKey();
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");
        return addAuthority(context, authority -> authority.addShape(authority.key, ProtectionShape.box(dimension, min, max)));
    }

    private static int addAuthorityWithDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey();
        return addAuthority(context, authority -> authority.addShape(authority.key, ProtectionShape.dimension(dimension)));
    }

    private static int addAuthority(CommandContext<ServerCommandSource> context, UnaryOperator<Authority> operator) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "authority");

        ServerCommandSource source = context.getSource();
        Leukocyte leukocyte = Leukocyte.get(source.getMinecraftServer());
        Authority authority = operator.apply(Authority.create(key));

        if (leukocyte.addAuthority(authority)) {
            if (authority.shapes.isEmpty()) {
                source.sendFeedback(new LiteralText("Added empty authority as '" + key + "'"), true);
            } else {
                source.sendFeedback(new LiteralText("Added authority as '" + key + "' with ").append(authority.shapes.displayShort()), true);
            }

            source.sendFeedback(
                    new LiteralText("Run ")
                            .append(new LiteralText("/protect shape start").formatted(Formatting.GRAY))
                            .append(" to include additional shapes in this authority, and ")
                            .append(new LiteralText("/protect set rule " + key + " <rule> <allow|deny>").formatted(Formatting.GRAY))
                            .append(" to set the rules on this authority"),
                    false
            );

            return Command.SINGLE_SUCCESS;
        } else {
            throw AUTHORITY_ALREADY_EXISTS.create(key);
        }
    }

    private static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Authority authority = AuthorityArgument.get(context, "authority");

        Leukocyte leukocyte = Leukocyte.get(context.getSource().getMinecraftServer());
        leukocyte.removeAuthority(authority.key);

        context.getSource().sendFeedback(new LiteralText("Removed authority " + authority.key), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int setRule(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Authority authority = AuthorityArgument.get(context, "authority");

        ProtectionRule rule = ProtectionRuleArgument.get(context, "rule");
        RuleResult result = RuleResultArgument.get(context, "result");

        authority.rules.put(rule, result);
        context.getSource().sendFeedback(new LiteralText("Set rule " + rule.getKey() + " = " + result.getKey() + " for " + authority.key), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int setLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Authority authority = AuthorityArgument.get(context, "authority");
        int level = IntegerArgumentType.getInteger(context, "level");

        Leukocyte leukocyte = Leukocyte.get(context.getSource().getMinecraftServer());

        Authority newAuthority = authority.withLevel(level);
        leukocyte.replaceAuthority(authority, newAuthority);

        context.getSource().sendFeedback(new LiteralText("Changed level of " + authority.key + " from " + authority.level + " to " + level), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int addPlayerExclusion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Authority authority = AuthorityArgument.get(context, "authority");
        Collection<GameProfile> players = GameProfileArgumentType.getProfileArgument(context, "player");

        int count = 0;
        for (GameProfile player : players) {
            if (authority.exclusions.addPlayer(player)) {
                count++;
            }
        }

        context.getSource().sendFeedback(new LiteralText("Added " + count + " player exclusions to " + authority.key), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int removePlayerExclusion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Authority authority = AuthorityArgument.get(context, "authority");
        Collection<GameProfile> players = GameProfileArgumentType.getProfileArgument(context, "player");

        int count = 0;
        for (GameProfile player : players) {
            if (authority.exclusions.removePlayer(player)) {
                count++;
            }
        }

        context.getSource().sendFeedback(new LiteralText("Removed " + count + " player exclusions from " + authority.key), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int addRoleExclusion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Authority authority = AuthorityArgument.get(context, "authority");
        String role = RoleArgument.get(context, "role");

        if (authority.exclusions.addRole(role)) {
            context.getSource().sendFeedback(new LiteralText("Added '" + role + "' exclusion to " + authority.key), true);
        } else {
            context.getSource().sendError(new LiteralText("'" + role + "' is already excluded"));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeRoleExclusion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Authority authority = AuthorityArgument.get(context, "authority");
        String role = RoleArgument.get(context, "role");

        if (authority.exclusions.removeRole(role)) {
            context.getSource().sendFeedback(new LiteralText("Removed '" + role + "' exclusion from " + authority.key), true);
        } else {
            context.getSource().sendError(new LiteralText("'" + role + "' is not excluded"));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listAuthorities(CommandContext<ServerCommandSource> context) {
        Leukocyte leukocyte = Leukocyte.get(context.getSource().getMinecraftServer());

        List<Authority> authorities = leukocyte.authorities()
                .filter(authority -> !authority.isTransient)
                .collect(Collectors.toList());

        if (authorities.isEmpty()) {
            context.getSource().sendError(new LiteralText("There are no authorities!"));
            return Command.SINGLE_SUCCESS;
        }

        MutableText text = new LiteralText("Listing " + authorities.size() + " registered authorities:\n");
        for (Authority authority : authorities) {
            text = text.append("  ").append(new LiteralText(authority.key).formatted(Formatting.AQUA)).append("@" + authority.level + ": ")
                    .append(authority.shapes.displayShort())
                    .append("\n");
        }

        context.getSource().sendFeedback(text, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int testRulesHere(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Leukocyte leukocyte = Leukocyte.get(source.getMinecraftServer());

        RuleSample sample = leukocyte.sample(RuleQuery.forPlayer(player));
        List<Authority> authorities = Lists.newArrayList(sample);
        if (authorities.isEmpty()) {
            source.sendError(new LiteralText("There are no authorities that apply to you at your current location!"));
            return Command.SINGLE_SUCCESS;
        }

        MutableText text = new LiteralText("Testing applicable rules at your current location:\n");

        text = text.append(" from authorities: ");
        for (int i = 0; i < authorities.size(); i++) {
            String tail = i < authorities.size() - 1 ? ", " : "\n\n";

            Authority authority = authorities.get(i);
            text.append(new LiteralText(authority.key).formatted(Formatting.AQUA)).append(tail);
        }

        boolean empty = true;
        for (ProtectionRule rule : ProtectionRule.REGISTRY) {
            for (Authority authority : authorities) {
                RuleResult result = authority.rules.test(rule);
                if (result != RuleResult.PASS) {
                    text = text.append("  ").append(new LiteralText(rule.getKey()).formatted(Formatting.AQUA))
                            .append(" = ").append(new LiteralText(result.getKey()).formatted(result.getFormatting()))
                            .append(" (" + authority.key + ")\n");
                    empty = false;
                    break;
                }
            }
        }

        if (empty) {
            text = text.append("  No rules applied!");
        }

        source.sendFeedback(text, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int displayAuthority(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Authority authority = AuthorityArgument.get(context, "authority");

        MutableText text = new LiteralText("Information for '" + authority.key + "':\n");
        text = text.append(" Level: ").append(new LiteralText(String.valueOf(authority.level)).formatted(Formatting.AQUA)).append("\n");
        text = text.append(" Shapes:\n").append(authority.shapes.displayList());

        if (!authority.rules.isEmpty()) {
            text.append(" Rules:\n").append(authority.rules.display());
        }

        context.getSource().sendFeedback(text, false);

        return Command.SINGLE_SUCCESS;
    }
}
