package xyz.nucleoid.leukocyte.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;afterBreak(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/item/ItemStack;)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void shouldDropItems(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (this.player.world.isClient) {
            return;
        }

        Leukocyte leukocyte = Leukocyte.get(this.player.server);

        RuleQuery query = RuleQuery.forPlayerAt(this.player, pos);
        if (leukocyte.denies(query, ProtectionRule.BLOCK_DROPS)) {
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "NEW", target = "net/minecraft/item/ItemUsageContext"))
    private void onUseItemOnBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {
        if (this.player.world.isClient) {
            return;
        }

        Leukocyte leukocyte = Leukocyte.get(this.player.server);

        RuleQuery query = RuleQuery.forPlayerAt(player, hit.getBlockPos());
        if (leukocyte.denies(query, ProtectionRule.PLACE)) {
            ci.setReturnValue(ActionResult.FAIL);
        }
    }
}
