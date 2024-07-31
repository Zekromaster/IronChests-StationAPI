package net.zekromaster.minecraft.ironchests

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