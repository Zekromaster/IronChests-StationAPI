@file:JvmName("UpgradeUtils")

package net.zekromaster.minecraft.ironchests.upgrades

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World
import net.modificationstation.stationapi.api.template.item.TemplateItem
import net.modificationstation.stationapi.api.util.Identifier

private val MID_UPGRADE = mutableSetOf<ChestBlockEntity>()

fun isMidUpgrade(blockEntity: BlockEntity) = MID_UPGRADE.contains(blockEntity)

abstract class ChestUpgrade(identifier: Identifier): TemplateItem(identifier) {
    fun upgrade(world: World, x: Int, y: Int, z: Int, player: PlayerEntity, blockEntity: ChestBlockEntity): Boolean {
        MID_UPGRADE.add(blockEntity)
        val retVal = innerUpgrade(world, x, y, z, player, blockEntity)
        MID_UPGRADE.remove(blockEntity)
        return retVal
    }

    abstract fun innerUpgrade(world: World, x: Int, y: Int, z: Int, player: PlayerEntity, blockEntity: ChestBlockEntity): Boolean
}

