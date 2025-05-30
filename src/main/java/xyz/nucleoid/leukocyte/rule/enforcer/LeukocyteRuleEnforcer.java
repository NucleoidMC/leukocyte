package xyz.nucleoid.leukocyte.rule.enforcer;

import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.ProtectionRuleMap;
import xyz.nucleoid.stimuli.event.DroppedItemsResult;
import xyz.nucleoid.stimuli.event.EventRegistrar;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockDropItemsEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockRandomTickEvent;
import xyz.nucleoid.stimuli.event.block.BlockTrampleEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.block.CoralDeathEvent;
import xyz.nucleoid.stimuli.event.block.DispenserActivateEvent;
import xyz.nucleoid.stimuli.event.block.FlowerPotModifyEvent;
import xyz.nucleoid.stimuli.event.block.FluidRandomTickEvent;
import xyz.nucleoid.stimuli.event.entity.EntityActivateDeathProtectionEvent;
import xyz.nucleoid.stimuli.event.entity.EntityShearEvent;
import xyz.nucleoid.stimuli.event.entity.EntitySpawnEvent;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;
import xyz.nucleoid.stimuli.event.item.ItemCraftEvent;
import xyz.nucleoid.stimuli.event.item.ItemPickupEvent;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerConsumeHungerEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerRegenerateEvent;
import xyz.nucleoid.stimuli.event.player.PlayerSpectateEntityEvent;
import xyz.nucleoid.stimuli.event.world.*;

import java.util.ArrayList;

public final class LeukocyteRuleEnforcer implements ProtectionRuleEnforcer {
    public static final LeukocyteRuleEnforcer INSTANCE = new LeukocyteRuleEnforcer();

    private LeukocyteRuleEnforcer() {
    }

    @Override
    public void applyTo(ProtectionRuleMap rules, EventRegistrar events) {
        this.applyBlockRules(rules, events);
        this.applyInteractionRules(rules, events);

        this.forRule(events, rules.test(ProtectionRule.ATTACK))
                .applySimple(PlayerAttackEntityEvent.EVENT, rule -> (attacker, hand, attacked, hitResult) -> rule);

        this.forRule(events, rules.test(ProtectionRule.PVP))
                .applySimple(PlayerAttackEntityEvent.EVENT, rule -> (attacker, hand, attacked, hitResult) -> {
                    return attacked instanceof PlayerEntity ? rule : EventResult.PASS;
                });

        this.forRule(events, rules.test(ProtectionRule.SPECTATE_ENTITIES))
                .applySimple(PlayerSpectateEntityEvent.EVENT, rule -> (player, target) -> rule);

        this.forRule(events, rules.test(ProtectionRule.CRAFTING))
                .applySimple(ItemCraftEvent.EVENT, rule -> (player, recipe) -> rule);

        this.forRule(events, rules.test(ProtectionRule.REGENERATION))
                .applySimple(PlayerRegenerateEvent.EVENT, rule -> (player, amount) -> rule);

        this.forRule(events, rules.test(ProtectionRule.HUNGER))
                .applySimple(PlayerConsumeHungerEvent.EVENT, rule -> (player, foodLevel, saturation, exhaustion) -> rule);

        this.forRule(events, rules.test(ProtectionRule.FALL_DAMAGE))
                .applySimple(PlayerDamageEvent.EVENT, rule -> {
                    return (player, source, amount) -> source.isIn(DamageTypeTags.IS_FALL) ? rule : EventResult.PASS;
                });

        this.forRule(events, rules.test(ProtectionRule.FIRE_DAMAGE))
                .applySimple(PlayerDamageEvent.EVENT, rule -> {
                    return (player, source, amount) -> source.isIn(DamageTypeTags.IS_FIRE) && !source.isOf(DamageTypes.LAVA) ? rule : EventResult.PASS;
                });

        this.forRule(events, rules.test(ProtectionRule.FREEZING_DAMAGE))
                .applySimple(PlayerDamageEvent.EVENT, rule -> {
                    return (player, source, amount) -> source.isIn(DamageTypeTags.IS_FREEZING) ? rule : EventResult.PASS;
                });

        this.forRule(events, rules.test(ProtectionRule.LAVA_DAMAGE))
                .applySimple(PlayerDamageEvent.EVENT, rule -> {
                    return (player, source, amount) -> source.isOf(DamageTypes.LAVA) ? rule : EventResult.PASS;
                });

        this.forRule(events, rules.test(ProtectionRule.DAMAGE))
                .applySimple(PlayerDamageEvent.EVENT, rule -> {
                    return (player, source, amount) -> !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) ? rule : EventResult.PASS;
                });

        this.forRule(events, rules.test(ProtectionRule.ACTIVATE_DEATH_PROTECTION))
                .applySimple(EntityActivateDeathProtectionEvent.EVENT, rule -> (player, source, stack) -> rule);

        this.forRule(events, rules.test(ProtectionRule.THROW_ITEMS))
                .applySimple(ItemThrowEvent.EVENT, rule -> (player, slot, stack) -> rule);
        this.forRule(events, rules.test(ProtectionRule.PICKUP_ITEMS))
                .applySimple(ItemPickupEvent.EVENT, rule -> (player, entity, stack) -> rule);

        this.forRule(events, rules.test(ProtectionRule.SPAWN_MONSTERS))
                .applySimple(EntitySpawnEvent.EVENT, rule -> entity -> entity instanceof Monster ? rule : EventResult.PASS);

        this.forRule(events, rules.test(ProtectionRule.SPAWN_ANIMALS))
                .applySimple(EntitySpawnEvent.EVENT, rule -> entity -> entity instanceof AnimalEntity ? rule : EventResult.PASS);

        this.forRule(events, rules.test(ProtectionRule.THROW_PROJECTILES))
                .applySimple(ItemUseEvent.EVENT, rule -> {
                    return (player, hand) -> {
                        ItemStack stack = player.getStackInHand(hand);
                        if (stack.isOf(Items.EGG) || stack.isOf(Items.SNOWBALL) || stack.isOf(Items.TRIDENT)) {
                            return ActionResult.FAIL;
                        }

                        return ActionResult.PASS;
                    };
                });

        this.forRule(events, rules.test(ProtectionRule.SHEAR_ENTITIES))
                .applySimple(EntityShearEvent.EVENT, rule -> (entity, player, hand, pos) -> rule);

        this.applyWorldRules(rules, events);
    }

    private void applyBlockRules(ProtectionRuleMap rules, EventRegistrar events) {
        this.forRule(events, rules.test(ProtectionRule.BREAK))
                .applySimple(BlockBreakEvent.EVENT, rule -> (player, world, pos) -> rule);

        this.forRule(events, rules.test(ProtectionRule.PLACE))
                .applySimple(BlockPlaceEvent.BEFORE, rule -> (player, world, pos, state, context) -> rule);

        this.forRule(events, rules.test(ProtectionRule.BLOCK_DROPS))
                .applySimple(BlockDropItemsEvent.EVENT, rule -> {
                    return (breaker, world, pos, state, dropStacks) -> {
                        return switch (rule) {
                            case ALLOW -> DroppedItemsResult.allow(dropStacks);
                            case DENY -> DroppedItemsResult.deny();
                            default -> DroppedItemsResult.pass(dropStacks);
                        };
                    };
                });

        this.forRule(events, rules.test(ProtectionRule.BLOCK_TRAMPLE))
                .applySimple(BlockTrampleEvent.EVENT, rule -> (entity, world, pos, from, to) -> rule);

        this.forRule(events, rules.test(ProtectionRule.EXPLOSION))
                .applySimple(ExplosionDetonatedEvent.EVENT, rule -> (explosion, blocksToDestroy) -> rule);

        this.forRule(events, rules.test(ProtectionRule.BLOCK_RANDOM_TICK))
                .applySimple(BlockRandomTickEvent.EVENT, rule -> (world, pos, state) -> rule);

        this.forRule(events, rules.test(ProtectionRule.FLUID_RANDOM_TICK))
                .applySimple(FluidRandomTickEvent.EVENT, rule -> (world, pos, state) -> rule);
    }

    private void applyInteractionRules(ProtectionRuleMap rules, EventRegistrar events) {
        var interact = rules.test(ProtectionRule.INTERACT);
        var interactBlocks = rules.test(ProtectionRule.INTERACT_BLOCKS).orElse(interact);
        var interactEntities = rules.test(ProtectionRule.INTERACT_ENTITIES).orElse(interact);
        var interactItems = rules.test(ProtectionRule.INTERACT_ITEMS).orElse(interact);

        this.forRule(events, interactBlocks)
                .applySimple(BlockUseEvent.EVENT, rule -> (player, hand, hitResult) -> rule.asActionResult());

        this.forRule(events, interactEntities)
                .applySimple(EntityUseEvent.EVENT, rule -> (player, entity, hand, hitResult) -> rule);

        this.forRule(events, interactItems)
                .applySimple(ItemUseEvent.EVENT, rule -> (player, hand) -> rule.asActionResult());
    }

    private void applyWorldRules(ProtectionRuleMap rules, EventRegistrar events) {
        this.forRule(events, rules.test(ProtectionRule.UNSTABLE_TNT))
                .applySimple(BlockPlaceEvent.AFTER, rule -> (player, world, pos, state) -> {
                            if (rule == EventResult.ALLOW && state.getBlock() == Blocks.TNT) {
                                TntBlock.primeTnt(player.getWorld(), pos);
                                player.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
                            }
                        }
                );

        this.forRule(events, rules.test(ProtectionRule.PORTALS))
                .applySimple(NetherPortalOpenEvent.EVENT, rule -> (world, lowerCorner) -> rule);

        this.forRule(events, rules.test(ProtectionRule.IGNITE_TNT))
                .applySimple(TntIgniteEvent.EVENT, rule -> (world, pos, igniter) -> rule);

        this.forRule(events, rules.test(ProtectionRule.MODIFY_FLOWER_POTS))
                .applySimple(FlowerPotModifyEvent.EVENT, rule -> (player, hand, hitResult) -> rule);

        this.forRule(events, rules.test(ProtectionRule.FIREWORK_EXPLODE))
                .applySimple(FireworkExplodeEvent.EVENT, rule -> firework -> rule);

        this.forRule(events, rules.test(ProtectionRule.DISPENSER_ACTIVATE))
                .applySimple(DispenserActivateEvent.EVENT, rule -> (world, pos, dispenser, slot, stack) -> rule);

        this.forRule(events, rules.test(ProtectionRule.SPAWN_WITHER))
                .applySimple(WitherSummonEvent.EVENT, rule -> (world, pos) -> rule);

        this.forRule(events, rules.test(ProtectionRule.FIRE_TICK))
                .applySimple(FireTickEvent.EVENT, rule -> (world, pos) -> rule);

        this.forRule(events, rules.test(ProtectionRule.FLUID_FLOW))
                .applySimple(FluidFlowEvent.EVENT, rule -> {
                    return (world, fluidPos, fluidBlock, flowDirection, flowTo, flowToBlock) -> rule;
                });

        this.forRule(events, rules.test(ProtectionRule.ICE_MELT))
                .applySimple(IceMeltEvent.EVENT, rule -> (world, pos) -> rule);

        this.forRule(events, rules.test(ProtectionRule.SNOW_FALL))
                .applySimple(SnowFallEvent.EVENT, rule -> (world, pos) -> rule);

        this.forRule(events, rules.test(ProtectionRule.CORAL_DEATH))
                .applySimple(CoralDeathEvent.EVENT, rule -> (world, pos, from, to) -> rule);
    }
}
