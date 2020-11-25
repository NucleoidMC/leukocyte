package xyz.nucleoid.leukocyte.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.AreaHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.leukocyte.ProtectionManager;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(AreaHelper.class)
public class AreaHelperMixin {
    @Shadow
    @Final
    private WorldAccess world;
    @Shadow
    @Nullable
    private BlockPos lowerCorner;

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void isValid(CallbackInfoReturnable<Boolean> ci) {
        if (!(this.world instanceof ServerWorldAccess) || this.lowerCorner == null) {
            return;
        }

        ServerWorld serverWorld = ((ServerWorldAccess) this.world).toServerWorld();

        ProtectionManager protection = ProtectionManager.get(serverWorld.getServer());

        RuleQuery query = RuleQuery.at(serverWorld, this.lowerCorner);
        if (protection.denies(query, ProtectionRule.PORTALS)) {
            ci.setReturnValue(false);
        }
    }
}
