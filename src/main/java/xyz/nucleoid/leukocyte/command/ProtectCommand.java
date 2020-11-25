package xyz.nucleoid.leukocyte.command;

import com.google.common.collect.Lists;
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
import xyz.nucleoid.leukocyte.command.argument.ProtectionRegionArgument;
import xyz.nucleoid.leukocyte.command.argument.ProtectionRuleArgument;
import xyz.nucleoid.leukocyte.command.argument.RuleResultArgument;
import xyz.nucleoid.leukocyte.region.ProtectionRegion;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;
import xyz.nucleoid.leukocyte.scope.ProtectionScope;

import java.util.List;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ProtectCommand {
    private static final DynamicCommandExceptionType REGION_ALREADY_EXISTS = new DynamicCommandExceptionType(id -> {
        return new LiteralMessage("Region with the id '" + id + "' already exists!");
    });

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // @formatter:off
        dispatcher.register(
            literal("protect")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("add")
                    .then(argument("region", StringArgumentType.string())
                    .executes(ProtectCommand::addGlobal)
                        .then(argument("dimension", DimensionArgumentType.dimension())
                            .executes(ProtectCommand::addDimension)
                                .then(argument("min", BlockPosArgumentType.blockPos())
                                .then(argument("max", BlockPosArgumentType.blockPos())
                                .executes(ProtectCommand::addBox)
                        ))
                    )
                ))
                .then(literal("remove")
                    .then(ProtectionRegionArgument.argument("region")
                    .executes(ProtectCommand::remove)
                ))
                .then(literal("set")
                    .then(literal("rule")
                        .then(ProtectionRegionArgument.argument("region")
                        .then(ProtectionRuleArgument.argument("rule")
                        .then(RuleResultArgument.argument("result")
                        .executes(ProtectCommand::setRule)
                    ))))
                    .then(literal("level")
                        .then(ProtectionRegionArgument.argument("region")
                        .then(argument("level", IntegerArgumentType.integer())
                        .executes(ProtectCommand::setLevel)
                    )))
                )
                .then(literal("display")
                    .then(ProtectionRegionArgument.argument("region")
                    .executes(ProtectCommand::displayRegion)
                ))
                .then(literal("list").executes(ProtectCommand::listRegions))
                .then(literal("test").then(literal("here").executes(ProtectCommand::testRegionsHere)))
        );
        // @formatter:on
    }

    private static int addBox(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "region");
        RegistryKey<World> dimension = DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey();
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");

        Leukocyte leukocyte = Leukocyte.get(context.getSource().getMinecraftServer());
        if (leukocyte.addRegion(new ProtectionRegion(key, ProtectionScope.box(dimension, min, max), 0))) {
            context.getSource().sendFeedback(new LiteralText("Added region in " + dimension.getValue() + " " + key), true);
        } else {
            throw REGION_ALREADY_EXISTS.create(key);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "region");
        RegistryKey<World> dimension = DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey();

        Leukocyte leukocyte = Leukocyte.get(context.getSource().getMinecraftServer());
        if (leukocyte.addRegion(new ProtectionRegion(key, ProtectionScope.dimension(dimension), 0))) {
            context.getSource().sendFeedback(new LiteralText("Added region in " + dimension.getValue() + " " + key), true);
        } else {
            throw REGION_ALREADY_EXISTS.create(key);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addGlobal(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "region");

        Leukocyte leukocyte = Leukocyte.get(context.getSource().getMinecraftServer());
        if (leukocyte.addRegion(new ProtectionRegion(key, ProtectionScope.global(), 0))) {
            context.getSource().sendFeedback(new LiteralText("Added global region " + key + "@"), true);
        } else {
            throw REGION_ALREADY_EXISTS.create(key);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ProtectionRegion region = ProtectionRegionArgument.get(context, "region");

        Leukocyte leukocyte = Leukocyte.get(context.getSource().getMinecraftServer());
        leukocyte.removeRegion(region.key);

        context.getSource().sendFeedback(new LiteralText("Removed region " + region.key), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int setRule(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ProtectionRegion region = ProtectionRegionArgument.get(context, "region");

        ProtectionRule rule = ProtectionRuleArgument.get(context, "rule");
        RuleResult result = RuleResultArgument.get(context, "result");

        region.rules.put(rule, result);
        context.getSource().sendFeedback(new LiteralText("Set rule " + rule.getKey() + " = " + result.getKey() + " for " + region.key), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int setLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ProtectionRegion region = ProtectionRegionArgument.get(context, "region");
        int level = IntegerArgumentType.getInteger(context, "level");

        Leukocyte leukocyte = Leukocyte.get(context.getSource().getMinecraftServer());

        ProtectionRegion newRegion = new ProtectionRegion(region.key, level, region.scope, region.rules.copy());
        leukocyte.replaceRegion(region, newRegion);

        context.getSource().sendFeedback(new LiteralText("Set level of " + region.key + " from " + region.level + " to " + level), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int listRegions(CommandContext<ServerCommandSource> context) {
        Leukocyte leukocyte = Leukocyte.get(context.getSource().getMinecraftServer());

        Set<String> regionKeys = leukocyte.getRegionKeys();
        if (regionKeys.isEmpty()) {
            context.getSource().sendError(new LiteralText("There are no regions!"));
            return Command.SINGLE_SUCCESS;
        }

        MutableText text = new LiteralText("Listing " + regionKeys.size() + " registered regions:\n");
        for (String regionKey : regionKeys) {
            ProtectionRegion region = leukocyte.getRegionByKey(regionKey);
            if (region == null) {
                continue;
            }

            text = text.append("  ").append(new LiteralText(regionKey).formatted(Formatting.AQUA)).append("@" + region.level + ": ")
                    .append(region.scope.display())
                    .append("\n");
        }

        context.getSource().sendFeedback(text, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int testRegionsHere(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Leukocyte leukocyte = Leukocyte.get(source.getMinecraftServer());

        RuleSample sample = leukocyte.sample(RuleQuery.forPlayer(player));
        List<ProtectionRegion> regions = Lists.newArrayList(sample);
        if (regions.isEmpty()) {
            source.sendError(new LiteralText("There are no regions at your current location!"));
            return Command.SINGLE_SUCCESS;
        }

        MutableText text = new LiteralText("Testing rules at your current location:\n");

        text = text.append(" from regions: ");
        for (int i = 0; i < regions.size(); i++) {
            String tail = i < regions.size() - 1 ? ", " : "\n\n";

            ProtectionRegion region = regions.get(i);
            text.append(new LiteralText(region.key).formatted(Formatting.AQUA)).append(tail);
        }

        boolean empty = true;
        for (ProtectionRule rule : ProtectionRule.REGISTRY) {
            // TODO: test for bypass
            for (ProtectionRegion region : regions) {
                RuleResult result = region.rules.test(rule);
                if (result != RuleResult.PASS) {
                    text = text.append("  ").append(new LiteralText(rule.getKey()).formatted(Formatting.AQUA))
                            .append(" = ").append(new LiteralText(result.getKey()).formatted(result.getFormatting()))
                            .append(" (" + region.key + ")\n");
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

    private static int displayRegion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ProtectionRegion region = ProtectionRegionArgument.get(context, "region");

        context.getSource().sendFeedback(
                new LiteralText("Information for '" + region.key + "':\n")
                        .append(" Level: ").append(new LiteralText(String.valueOf(region.level)).formatted(Formatting.AQUA)).append("\n")
                        .append(" Scope: ").append(region.scope.display()).append("\n")
                        .append(" Rules:\n").append(region.rules.display()),
                false
        );

        return Command.SINGLE_SUCCESS;
    }
}
