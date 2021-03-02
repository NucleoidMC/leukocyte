package xyz.nucleoid.leukocyte.mixin.rule;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.IceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(IceBlock.class)
public class IceBlockMixin {
    @Inject(method = "melt", at = @At("HEAD"), cancellable = true)
    private void applyIceMeltProtectionRule(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
        Leukocyte leukocyte = Leukocyte.byWorld(world);
        if (leukocyte != null) {
            RuleQuery query = RuleQuery.at(world, pos);
            if (leukocyte.denies(query, ProtectionRule.ICE_MELT)) {
                ci.cancel();
            }
        }
    }
}
