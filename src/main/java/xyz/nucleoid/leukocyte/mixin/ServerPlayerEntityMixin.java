package xyz.nucleoid.leukocyte.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.leukocyte.ProtectionManager;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "isPvpEnabled", at = @At("HEAD"), cancellable = true)
    private void testPvpEnabled(CallbackInfoReturnable<Boolean> ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        ProtectionManager protection = ProtectionManager.get(self.server);

        RuleResult result = protection.test(self.world, self.getBlockPos(), ProtectionRule.PVP, self);
        if (result == RuleResult.ALLOW) {
            ci.setReturnValue(true);
        } else if (result == RuleResult.DENY) {
            ci.setReturnValue(false);
        }
    }
}
