package xyz.nucleoid.leukocyte.mixin.rule;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "isPvpEnabled", at = @At("HEAD"), cancellable = true)
    private void testPvpEnabled(CallbackInfoReturnable<Boolean> ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        Leukocyte leukocyte = Leukocyte.get(self.server);

        RuleQuery query = RuleQuery.forPlayer(self);
        RuleResult result = leukocyte.test(query, ProtectionRule.PVP);
        if (result == RuleResult.ALLOW) {
            ci.setReturnValue(true);
        } else if (result == RuleResult.DENY) {
            ci.setReturnValue(false);
        }
    }
}
