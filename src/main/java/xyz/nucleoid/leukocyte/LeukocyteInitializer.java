package xyz.nucleoid.leukocyte;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import xyz.nucleoid.leukocyte.command.ProtectCommand;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

public final class LeukocyteInitializer implements ModInitializer  {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            ProtectCommand.register(dispatcher);
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
            Leukocyte leukocyte = Leukocyte.byWorld(world);
            if (leukocyte != null) {
                RuleQuery query = RuleQuery.forPlayerAt(player, pos);
                return !leukocyte.denies(query, ProtectionRule.BREAK);
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            Leukocyte leukocyte = Leukocyte.byWorld(world);
            if (leukocyte != null) {
                RuleQuery query = RuleQuery.forPlayerAt(player, hit.getBlockPos());
                RuleSample sample = leukocyte.sample(query);

                RuleResult result = sample.test(ProtectionRule.INTERACT_BLOCKS).orElse(sample.test(ProtectionRule.INTERACT));
                if (result == RuleResult.DENY) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            Leukocyte leukocyte = Leukocyte.byWorld(world);
            if (leukocyte != null) {
                RuleQuery query = RuleQuery.forPlayer(player);
                if (leukocyte.denies(query, ProtectionRule.INTERACT)) {
                    return TypedActionResult.fail(player.getStackInHand(hand));
                }
            }

            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            Leukocyte leukocyte = Leukocyte.byWorld(world);
            if (leukocyte != null) {
                RuleQuery query = RuleQuery.forPlayerAt(player, entity.getBlockPos());
                RuleSample sample = leukocyte.sample(query);

                RuleResult result = sample.test(ProtectionRule.INTERACT_ENTITIES).orElse(sample.test(ProtectionRule.INTERACT));
                if (result == RuleResult.DENY) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            Leukocyte leukocyte = Leukocyte.byWorld(world);
            if (leukocyte != null) {
                RuleQuery query = RuleQuery.forPlayerAt(player, entity.getBlockPos());
                if (leukocyte.denies(query, ProtectionRule.ATTACK)) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }
}
