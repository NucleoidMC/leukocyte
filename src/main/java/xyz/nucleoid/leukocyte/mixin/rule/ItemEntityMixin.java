package xyz.nucleoid.leukocyte.mixin.rule;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.ItemEntity;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Shadow
    @Final
    private int pickupDelay;

    @Redirect(method = "onPlayerCollision", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ItemEntity;pickupDelay:I"))
    private int applyCollisionItemsProtectionRule(ItemEntity self) {
        Leukocyte leukocyte = Leukocyte.byWorld(self.world);
        if (leukocyte != null) {
            RuleQuery query = RuleQuery.at(self.world, self.getBlockPos());
            if (leukocyte.denies(query, ProtectionRule.PICKUP_ITEMS)) {
                // pickupDelay != 0 prevents pickup
                return 1;
            }
        }

        return this.pickupDelay;
    }

    @Inject(method = "cannotPickup", at = @At("HEAD"), cancellable = true)
    private void applyExternalPickupItemsProtectionRule(CallbackInfoReturnable<Boolean> ci) {
        ItemEntity self = (ItemEntity) (Object) this;

        Leukocyte leukocyte = Leukocyte.byWorld(self.world);
        if (leukocyte != null) {
            RuleQuery query = RuleQuery.at(self.world, self.getBlockPos());
            if (leukocyte.denies(query, ProtectionRule.PICKUP_ITEMS)) {
                ci.setReturnValue(true);
            }
        }
    }
}
