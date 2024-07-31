package net.zekromaster.minecraft.ironchests.mixin;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestBlockEntity.class)
public interface ChestInventoryAccessor {

    @Accessor
    ItemStack[] getInventory();

    @Accessor("inventory")
    void setInventory(ItemStack[] newInventory);

}
