package net.zekromaster.minecraft.ironchests.mixin;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.zekromaster.minecraft.ironchests.ChestUpgrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public class ChestMixin {

    @Unique
    private boolean midUpgrade = true;

    @Inject(method = "onUse", at=@At("HEAD"), cancellable = true)
    void onUseMixin(World world, int x, int y, int z, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        var chest = (ChestBlockEntity) world.getBlockEntity(x, y, z);
        var hand = player.getHand();
        if (hand != null) {
            if (hand.getItem() instanceof ChestUpgrade upgrade) {
                this.midUpgrade = true;
                if (upgrade.upgrade(world, x, y, z, player, chest)) {
                    hand.count--;
                }
                this.midUpgrade = false;
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "onBreak", at=@At("HEAD"), cancellable = true)
    void onBreakMixin(World x, int y, int z, int par4, CallbackInfo ci) {
        if (this.midUpgrade) {
            ci.cancel();
        }
    }

}
