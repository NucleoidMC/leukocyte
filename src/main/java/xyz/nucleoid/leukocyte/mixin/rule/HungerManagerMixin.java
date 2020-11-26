package xyz.nucleoid.leukocyte.mixin.rule;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Shadow
    private float exhaustion;

    @Shadow
    private float foodSaturationLevel;

    @Inject(method = "update", at = @At("HEAD"))
    private void update(PlayerEntity player, CallbackInfo ci) {
        if (!player.world.isClient && this.exhaustion > 4.0F || this.foodSaturationLevel > 0.0F) {
            Leukocyte leukocyte = Leukocyte.byWorld(player.world);

            if (leukocyte != null) {
                RuleQuery query = RuleQuery.forPlayer(player);
                if (leukocyte.denies(query, ProtectionRule.HUNGER)) {
                    this.exhaustion = 0.0F;
                    this.foodSaturationLevel = 0.0F;
                }
            }
        }
    }
}
