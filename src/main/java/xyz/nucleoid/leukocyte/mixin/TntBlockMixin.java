package xyz.nucleoid.leukocyte.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.leukocyte.ProtectionManager;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(TntBlock.class)
public class TntBlockMixin {
    @Inject(method = "onBlockAdded", at = @At("HEAD"))
    private void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved, CallbackInfo ci) {
        if (world instanceof ServerWorld) {
            ProtectionManager protection = ProtectionManager.get(world.getServer());

            RuleQuery query = RuleQuery.at(world, pos);
            if (protection.allows(query, ProtectionRule.UNSTABLE_TNT)) {
                TntBlock.primeTnt(world, pos);
                world.removeBlock(pos, false);
            }
        }
    }
}
