package net.zekromaster.minecraft.ironchests

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import org.lwjgl.opengl.GL11


private fun IronChestMaterial.gui() =
    when (this) {
        IronChestMaterial.IRON -> GUIType.IRON
        IronChestMaterial.GOLD -> GUIType.GOLD
        IronChestMaterial.DIAMOND -> GUIType.DIAMOND
    }


private enum class GUIType(val material: IronChestMaterial, val width: Int, val height: Int, val asset: String) {
    IRON(IronChestMaterial.IRON, 184, 202, "ironchest.png"),
    GOLD(IronChestMaterial.GOLD, 184, 256, "goldchest.png"),
    DIAMOND(IronChestMaterial.DIAMOND, 238, 256, "diamondchest.png");

    fun handler(playerInventory: Inventory, inventory: Inventory): ScreenHandler =
        IronChestScreenHandler(this, playerInventory, inventory, width, height)

}

private class IronChestScreenHandler(
    type: GUIType,
    playerInventory: Inventory,
    val chestInventory: Inventory,
    x: Int,
    y: Int
): ScreenHandler() {
    init {
        for (row in 0 until type.material.grid.rows) {
            for (col in 0 until type.material.grid.columns) {
                addSlot(Slot(chestInventory, col + (row * type.material.grid.columns), 12 + (col * 18), 8 + (row * 18)))
            }
        }

        val leftCol: Int = ((x - 162) / 2) + 1
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                addSlot(Slot(playerInventory, col + row * 9 + 9, leftCol + col * 18, y - (4 - row) * 18 - 10))
            }
        }

        for (hotbarSlot in 0 until 9) {
            addSlot(Slot(playerInventory, hotbarSlot,leftCol + hotbarSlot * 18, y - 24))
        }
    }


    override fun canUse(player: PlayerEntity): Boolean =
        chestInventory.canPlayerUse(player)
}

@Environment(EnvType.CLIENT)
class IronChestScreen(
    private val playerInventory: Inventory,
    private val inventory: Inventory,
    material: IronChestMaterial
) : HandledScreen(run {
    val gui = material.gui()
    IronChestScreenHandler(gui, playerInventory, inventory, gui.width, gui.height)
}) {
    private val guiType = material.gui()

    init {
        this.field_155 = false
        backgroundWidth = guiType.width
        backgroundHeight = guiType.height
    }

    override fun drawBackground(tickDelta: Float) {
        val background = minecraft.textureManager.getTextureId("/assets/ironchests/stationapi/textures/gui/${guiType.asset}")
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft.textureManager.bindTexture(background)
        val x = (this.width - this.backgroundWidth) / 2
        val y = (this.height - this.backgroundHeight) / 2
        this.drawTexture(x, y, 0, 0, this.backgroundWidth, this.backgroundHeight)
    }
}
