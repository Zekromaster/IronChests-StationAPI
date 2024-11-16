package net.zekromaster.minecraft.ironchests.mixin;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.zekromaster.minecraft.ironchests.upgrades.ChestUpgrade;
import net.zekromaster.minecraft.ironchests.upgrades.UpgradeStorage;
import net.zekromaster.minecraft.ironchests.upgrades.UpgradeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ChestBlock.class)
public class ChestMixin {

    @Shadow private Random random;

    @Inject(method = "onUse", at=@At("HEAD"), cancellable = true)
    void onUseMixin(World world, int x, int y, int z, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        var chest = (ChestBlockEntity) world.getBlockEntity(x, y, z);
        var hand = player.getHand();
        if (hand != null) {
            if (hand.getItem() instanceof ChestUpgrade upgrade) {
                if (upgrade.upgrade(world, x, y, z, player, chest)) {
                    hand.count--;
                }
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "onBreak", at=@At("HEAD"), cancellable = true)
    void onBreakMixin(World world, int x, int y, int z, CallbackInfo ci) {
        if (UpgradeUtils.isMidUpgrade(world.getBlockEntity(x, y, z))) {
            ci.cancel();
        }
    }

    @Inject(
        method = "onBreak",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockWithEntity;onBreak(Lnet/minecraft/world/World;III)V")
    )
    void upgradeStorageMixin(World world, int x, int y, int z, CallbackInfo ci) {
        var upgradeStorage = UpgradeStorage.capability().get(world, x, y, z, null);
        if (upgradeStorage != null) {
            for (var upgrade: upgradeStorage.getStorage()) {
                var dropX = this.random.nextFloat() * 0.8f + 0.1f;
                var dropY = this.random.nextFloat() * 0.8f + 0.1f;
                var dropZ = this.random.nextFloat() * 0.8f + 0.1f;
                var drop = new ItemEntity(
                    world,
                    (x + dropX),
                    (y + dropY),
                    (z + dropZ),
                    upgrade
                );
                var velocityScale = 0.05f;
                drop.velocityX = (this.random.nextGaussian() * velocityScale);
                drop.velocityY = (this.random.nextGaussian() * velocityScale + 0.2f);
                drop.velocityZ = (this.random.nextGaussian() * velocityScale);
                world.spawnEntity(drop);
            }
        }
    }
}
