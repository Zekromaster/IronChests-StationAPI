package net.zekromaster.minecraft.ironchests.mixin;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.zekromaster.minecraft.ironchests.IronChestsBlockStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Redirect(
        method = "explode",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockId(III)I")
    )
    int blastResistance(World instance, int x, int y, int z) {
        var blockState = instance.getBlockState(x, y, z);
        if (blockState.contains(IronChestsBlockStates.HAS_OBSIDIAN_UPGRADE) && blockState.get(IronChestsBlockStates.HAS_OBSIDIAN_UPGRADE)) {
            return Block.OBSIDIAN.id;
        }
        return blockState.getBlock().id;
    }

}
