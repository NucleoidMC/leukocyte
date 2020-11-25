package xyz.nucleoid.leukocyte.mixin;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.leukocyte.ProtectionManager;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Shadow
    private float exhaustion;

    @Shadow
    private float foodSaturationLevel;

    @Inject(method = "update", at = @At("HEAD"))
    private void update(PlayerEntity player, CallbackInfo ci) {
        if (player.world.isClient || !(player instanceof ServerPlayerEntity)) {
            return;
        }

        if (this.exhaustion > 4.0F || this.foodSaturationLevel > 0.0F) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            ProtectionManager protection = ProtectionManager.get(serverPlayer.server);

            RuleResult result = protection.test(player.world, player.getBlockPos(), ProtectionRule.HUNGER, serverPlayer);
            if (result == RuleResult.DENY) {
                this.exhaustion = 0.0F;
                this.foodSaturationLevel = 0.0F;
            }
        }
    }
}
