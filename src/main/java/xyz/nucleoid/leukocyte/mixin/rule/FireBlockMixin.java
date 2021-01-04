package xyz.nucleoid.leukocyte.mixin.rule;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

import java.util.Random;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    private void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        Leukocyte leukocyte = Leukocyte.byWorld(world);
        if (leukocyte != null) {
            RuleQuery query = RuleQuery.at(world, pos);
            if (leukocyte.denies(query, ProtectionRule.FIRE_TICK)) {
                ci.cancel();
            }
        }
    }
}
