@file:JvmName("IronChests")

package net.zekromaster.minecraft.ironchests

import net.minecraft.entity.player.PlayerEntity
import net.modificationstation.stationapi.api.state.property.BooleanProperty
import net.modificationstation.stationapi.api.state.property.EnumProperty
import net.modificationstation.stationapi.api.util.math.Direction
import java.util.function.Predicate
import kotlin.math.floor

object IronChestsBlockStates {
    @JvmField
    val FACING: EnumProperty<Direction> = EnumProperty.of("facing", Direction::class.java, Predicate { it.axis.isHorizontal })
    @JvmField
    val HAS_OBSIDIAN_UPGRADE: BooleanProperty = BooleanProperty.of("obsidian")
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