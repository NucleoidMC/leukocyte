package xyz.nucleoid.leukocyte.mixin.rule;

import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    @Inject(method = "onLandedUpon", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FarmlandBlock;setToDirt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void breakFarmland(World world, BlockPos pos, Entity entity, float distance, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity) {
            Leukocyte leukocyte = Leukocyte.byWorld(world);
            if (leukocyte != null) {
                RuleQuery query = RuleQuery.forPlayerAt((ServerPlayerEntity) entity, pos);
                if (leukocyte.denies(query, ProtectionRule.BREAK)) {
                    ci.cancel();
                }
            }
        }
    }
}
