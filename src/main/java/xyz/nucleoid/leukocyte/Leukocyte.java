package xyz.nucleoid.leukocyte;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.leukocyte.command.ProtectCommand;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

public final class Leukocyte implements ModInitializer {
    public static final String ID = "leukocyte";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            ProtectCommand.register(dispatcher);
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
            if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
                ProtectionManager protection = ProtectionManager.get(world.getServer());
                RuleQuery query = RuleQuery.forPlayerAt((ServerPlayerEntity) player, pos);
                return !protection.denies(query, ProtectionRule.BREAK);
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
                ProtectionManager protection = ProtectionManager.get(world.getServer());
                BlockPos pos = hit.getBlockPos();

                RuleQuery query = RuleQuery.forPlayerAt((ServerPlayerEntity) player, pos);
                RuleSample sample = protection.sample(query);

                RuleResult result = sample.test(ProtectionRule.INTERACT_BLOCKS).orElse(sample.test(ProtectionRule.INTERACT));
                if (result == RuleResult.DENY) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
                ProtectionManager protection = ProtectionManager.get(world.getServer());

                RuleQuery query = RuleQuery.forPlayer((ServerPlayerEntity) player);
                if (protection.denies(query, ProtectionRule.INTERACT)) {
                    return TypedActionResult.fail(player.getStackInHand(hand));
                }
            }

            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
                ProtectionManager protection = ProtectionManager.get(world.getServer());

                RuleQuery query = RuleQuery.forPlayerAt((ServerPlayerEntity) player, entity.getBlockPos());
                RuleSample sample = protection.sample(query);

                RuleResult result = sample.test(ProtectionRule.INTERACT_ENTITIES).orElse(sample.test(ProtectionRule.INTERACT));
                if (result == RuleResult.DENY) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
                ProtectionManager protection = ProtectionManager.get(world.getServer());

                RuleQuery query = RuleQuery.forPlayerAt(((ServerPlayerEntity) player), entity.getBlockPos());
                if (protection.denies(query, ProtectionRule.ATTACK)) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }
}
