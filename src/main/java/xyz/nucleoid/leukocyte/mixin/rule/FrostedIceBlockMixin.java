package xyz.nucleoid.leukocyte.mixin.rule;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(FrostedIceBlock.class)
public class FrostedIceBlockMixin {
    @Inject(method = "increaseAge", at = @At("HEAD"), cancellable = true)
    private void applyFrostedIceMeltProtectionRule(BlockState state, World world, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        Leukocyte leukocyte = Leukocyte.byWorld(world);
        if (leukocyte != null) {
            RuleQuery query = RuleQuery.at(world, pos);
            if (leukocyte.denies(query, ProtectionRule.ICE_MELT)) {
                ci.setReturnValue(false);
            }
        }
    }
}
