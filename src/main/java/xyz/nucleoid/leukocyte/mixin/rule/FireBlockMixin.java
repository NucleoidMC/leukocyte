package xyz.nucleoid.leukocyte.mixin.rule;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

import java.util.Random;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean test(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule, BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Leukocyte leukocyte = Leukocyte.byWorld(world);
        RuleQuery query = RuleQuery.at(world, pos);

        RuleResult result = leukocyte.test(query, ProtectionRule.FIRE_TICK);
        if (result.isDefinitive()) {
            return result == RuleResult.ALLOW;
        }

        return gameRules.getBoolean(GameRules.DO_FIRE_TICK);
    }
}
