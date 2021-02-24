package xyz.nucleoid.leukocyte.mixin.rule;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(FlowableFluid.class)
public class FlowableFluidMixin {
    @Inject(method = "canFlow", at = @At("RETURN"), cancellable = true)
    public void applyFluidFlowGameRule(BlockView blockView, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo, BlockState flowToBlockState, FluidState fluidState, Fluid fluid, CallbackInfoReturnable<Boolean> ci) {
        if (!(blockView instanceof World)) return;
        World world = (World) blockView;

        Leukocyte leukocyte = Leukocyte.byWorld(world);
        if (leukocyte != null) {
            RuleQuery query = RuleQuery.at(world, fluidPos);
            if (leukocyte.denies(query, ProtectionRule.FLUID_FLOW)) {
                ci.setReturnValue(false);
            }
        }
    }
}
