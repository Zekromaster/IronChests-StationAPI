package net.zekromaster.minecraft.ironchests

import net.minecraft.entity.player.PlayerEntity
import net.modificationstation.stationapi.api.util.math.Direction
import kotlin.math.floor

data class IronChestGrid(val rows: Int, val columns: Int) {
    val size = rows * columns
}

enum class IronChestMaterial(val id: String, val grid: IronChestGrid) {
    IRON("iron", IronChestGrid(6, 9)),
    GOLD("gold", IronChestGrid(9,9 )),
    DIAMOND("diamond", IronChestGrid(9, 12));

    val size = grid.size
    val chestName: String = "${id.replaceFirstChar(Char::uppercaseChar)} Chest"

    companion object {
        @JvmStatic
        fun from(id: String): IronChestMaterial =
            when (id) {
                "gold" -> GOLD
                "diamond" -> DIAMOND
                else -> IRON
            }

    }
}

fun PlayerEntity.placementFacing(): Direction {
    val direction = floor((this.yaw * 4.0f / 360.0f).toDouble() + 0.5).toInt() and 3
    return when (direction) {
        0 -> Direction.NORTH
        1 -> Direction.EAST
        2 -> Direction.SOUTH
        3 -> Direction.WEST
        else -> Direction.NORTH
    }
}
