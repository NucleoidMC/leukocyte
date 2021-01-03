package xyz.nucleoid.leukocyte.mixin.rule;

import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(TntBlock.class)
public class TntBlockMixin {
    @Inject(method = "onBlockAdded", at = @At("HEAD"))
    private void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved, CallbackInfo ci) {
        Leukocyte leukocyte = Leukocyte.byWorld(world);
        if (leukocyte != null) {
            RuleQuery query = RuleQuery.at(world, pos);
            if (leukocyte.allows(query, ProtectionRule.UNSTABLE_TNT)) {
                TntBlock.primeTnt(world, pos);
                world.removeBlock(pos, false);
            }
        }
    }

    @Inject(method = "primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private static void primeTnt(World world, BlockPos pos, LivingEntity igniter, CallbackInfo ci) {
        Leukocyte leukocyte = Leukocyte.byWorld(world);
        if (leukocyte != null) {
            RuleQuery query = RuleQuery.at(world, pos);
            if (leukocyte.denies(query, ProtectionRule.IGNITE_TNT)) {
                ci.cancel();
            }
        }
    }
}
