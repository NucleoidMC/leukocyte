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
                RuleResult result = protection.test(world, pos, ProtectionRule.BREAK, (ServerPlayerEntity) player);
                return result != RuleResult.DENY;
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
                ProtectionManager protection = ProtectionManager.get(world.getServer());
                BlockPos pos = hit.getBlockPos();
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                // TODO: we can optimize double checks by reusing the same applicable regions
                RuleResult result = protection.test(world, pos, ProtectionRule.INTERACT_BLOCKS, serverPlayer);
                if (result == RuleResult.DENY) {
                    return ActionResult.FAIL;
                }

                result = protection.test(world, pos, ProtectionRule.INTERACT, serverPlayer);
                if (result == RuleResult.DENY) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
                ProtectionManager protection = ProtectionManager.get(world.getServer());

                RuleResult result = protection.test(world, player.getBlockPos(), ProtectionRule.INTERACT, (ServerPlayerEntity) player);
                if (result == RuleResult.DENY) {
                    return TypedActionResult.fail(player.getStackInHand(hand));
                }
            }

            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
                ProtectionManager protection = ProtectionManager.get(world.getServer());
                BlockPos pos = entity.getBlockPos();
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                RuleResult result = protection.test(world, pos, ProtectionRule.INTERACT_ENTITIES, serverPlayer);
                if (result == RuleResult.DENY) {
                    return ActionResult.FAIL;
                }

                result = protection.test(world, pos, ProtectionRule.INTERACT, serverPlayer);
                if (result == RuleResult.DENY) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
                ProtectionManager protection = ProtectionManager.get(world.getServer());

                RuleResult result = protection.test(world, entity.getBlockPos(), ProtectionRule.ATTACK, (ServerPlayerEntity) player);
                if (result == RuleResult.DENY) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }
}
