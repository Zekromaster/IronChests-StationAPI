package net.zekromaster.minecraft.ironchests

import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.item.ItemStack
import net.zekromaster.minecraft.ironchests.mixin.ChestInventoryAccessor

fun ChestBlockEntity.inventory(): Array<ItemStack?> =
    (this as ChestInventoryAccessor).inventory

fun ChestBlockEntity.inventory(inv: Array<ItemStack?>) {
    (this as ChestInventoryAccessor).inventory = inv
}