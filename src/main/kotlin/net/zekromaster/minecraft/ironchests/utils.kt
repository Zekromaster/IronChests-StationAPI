@file:JvmName("IronChests")

package net.zekromaster.minecraft.ironchests

import net.minecraft.entity.player.PlayerEntity
import net.modificationstation.stationapi.api.state.property.BooleanProperty
import net.modificationstation.stationapi.api.state.property.EnumProperty
import net.modificationstation.stationapi.api.util.math.Direction
import net.zekromaster.minecraft.ironchests.IronChestsBlockEntrypoint.DIAMOND_CHEST
import net.zekromaster.minecraft.ironchests.IronChestsBlockEntrypoint.GOLD_CHEST
import net.zekromaster.minecraft.ironchests.IronChestsBlockEntrypoint.IRON_CHEST
import java.util.function.Predicate
import kotlin.math.floor

enum class IronChestMaterial(val id: String, val rows: Int, val columns: Int) {
    IRON("iron", 6, 9),
    GOLD("gold", 9,9),
    DIAMOND("diamond", 9, 12);

    val size = rows * columns
    val chestName: String = "${id.replaceFirstChar(Char::uppercaseChar)} Chest"

    fun getBlock() =
        when (this) {
            IRON -> IRON_CHEST
            GOLD -> GOLD_CHEST
            DIAMOND -> DIAMOND_CHEST
        }

    fun createBlockEntity() =
        IronChestBlockEntity(this)

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