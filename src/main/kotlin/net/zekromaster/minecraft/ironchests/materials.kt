package net.zekromaster.minecraft.ironchests

import net.modificationstation.stationapi.api.util.Identifier
import net.modificationstation.stationapi.api.util.Namespace
import net.zekromaster.minecraft.ironchests.IronChestsBlockEntrypoint.DIAMOND_CHEST
import net.zekromaster.minecraft.ironchests.IronChestsBlockEntrypoint.GOLD_CHEST
import net.zekromaster.minecraft.ironchests.IronChestsBlockEntrypoint.IRON_CHEST

private val LOOKUP: MutableMap<Identifier, IronChestMaterial> = mutableMapOf()

class IronChestMaterial(
    val id: Identifier,
    val rows: Int,
    val columns: Int,
    val displayName: String,
    private val blockProvider: () -> IronChestBlock,
) {

    val size = rows * columns
    val chestName: String = "$displayName Chest"

    fun getBlock(): IronChestBlock = blockProvider()

    init {
        LOOKUP[id] = this
    }

    fun createBlockEntity() =
        IronChestBlockEntity(this)

    companion object {
        // Had to use the MINECRAFT namespace for backwards compatibility
        val IRON = IronChestMaterial(
            id = Namespace.MINECRAFT.id("iron"),
            rows = 6,
            columns = 9,
            displayName = "Iron",
            blockProvider = { IRON_CHEST }
        )
        val GOLD = IronChestMaterial(
            id = Namespace.MINECRAFT.id("gold"),
            rows = 9,
            columns = 9,
            displayName = "Gold",
            blockProvider = { GOLD_CHEST }
        )
        val DIAMOND = IronChestMaterial(
            id = Namespace.MINECRAFT.id("diamond"),
            rows = 9,
            columns = 12,
            displayName = "Diamond",
            blockProvider = { DIAMOND_CHEST }
        )

        fun from(id: Identifier) = LOOKUP[id]

        fun from(string: String) = from(Identifier.of(string))
    }
}