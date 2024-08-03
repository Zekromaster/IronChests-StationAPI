package net.zekromaster.minecraft.ironchests

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.modificationstation.stationapi.api.event.registry.GuiHandlerRegistryEvent
import net.modificationstation.stationapi.api.util.Identifier
import org.lwjgl.opengl.GL11
import uk.co.benjiweber.expressions.tuple.BiTuple
import java.util.function.BiFunction
import java.util.function.Supplier
import kotlin.math.max

internal object IronChestsGUIEntrypoint {

    private object OpenInventory: BiFunction<PlayerEntity, Inventory, Screen> {
        override fun apply(player: PlayerEntity, inventory: Inventory): Screen =
            IronChestScreen(player.inventory, inventory as IronChestBlockEntity)
    }

    @EventListener
    fun registerGUIs(event: GuiHandlerRegistryEvent) {
        event.registry.registerValueNoMessage(
            Identifier.of("ironchests:iron_chest"), BiTuple.of(
            OpenInventory,
            Supplier { IronChestBlockEntity() }
        ))
    }
}

internal class IronChestScreenHandler(
    playerInventory: Inventory,
    private val chest: IronChestBlockEntity,
): ScreenHandler() {
    val screenWidth = max(24 + (chest.material.columns * 18), 24 + (9*18))
    val screenHeight = 16 + (chest.material.rows * 18) + 80

    init {
        val rowLength = 18 * chest.material.columns
        val startX = (screenWidth - rowLength) / 2 + 1

        for (row in 0 until chest.material.rows) {
            for (column in 0 until chest.material.columns) {
                addSlot(Slot(chest, column + (row * chest.material.columns), startX + (column * 18), 9 + (row * 18)))
            }
        }

        playerInventory.draw(xOffset = ((screenWidth - 162) / 2) + 1, screenHeight = screenHeight)
    }

    private fun Inventory.draw(xOffset: Int, screenHeight: Int) {
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                addSlot(Slot(this, col + row * 9 + 9, xOffset + col * 18, screenHeight - (4 - row) * 18 - 11))
            }
        }

        for (hotbarSlot in 0 until 9) {
            addSlot(Slot(this, hotbarSlot,xOffset + hotbarSlot * 18, screenHeight - 25))
        }
    }


    override fun canUse(player: PlayerEntity): Boolean =
        chest.canPlayerUse(player)
}

@Environment(EnvType.CLIENT)
private class IronChestScreen(
    playerInventory: Inventory,
    val chest: IronChestBlockEntity,
) : HandledScreen(IronChestScreenHandler(playerInventory, chest)) {
    init {
        this.field_155 = false
        backgroundWidth = (container as IronChestScreenHandler).screenWidth
        backgroundHeight = (container as IronChestScreenHandler).screenHeight
    }

    override fun drawBackground(tickDelta: Float) {
        val x = (this.width - this.backgroundWidth) / 2
        val y = (this.height - this.backgroundHeight) / 2
        tiledGUI(x, y, this.backgroundWidth, this.backgroundHeight)
    }

    private fun tiledGUI(x: Int, y: Int, width: Int, height: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        val lastXTile = x+width-8
        val lastYTile = y+height-8

        val texture = minecraft.textureManager.getTextureId("/assets/ironchests/stationapi/textures/gui/chest.png")
        minecraft.textureManager.bindTexture(texture)

        // Fill
        for (drawX in x+8 until lastXTile step 8) {
            for (drawY in y+8 until lastYTile step 8) {
                this.drawTexture(drawX, drawY, 4, 4, 8, 8)
            }
        }

        // Top
        for (drawX in x+8 until lastXTile step 8) {
            this.drawTexture(drawX, y, 4, 0, 8, 8)
        }
        // Bottom
        for (drawX in x+8 until lastXTile step 8) {
            this.drawTexture(drawX, lastYTile, 4, 8, 8, 8)
        }
        // Left
        for (drawY in y+8 until lastYTile step 8) {
            this.drawTexture(x, drawY, 0, 4, 8, 8)
        }
        // Right
        for (drawY in y+8 until lastYTile step 8) {
            this.drawTexture(lastXTile, drawY, 8, 4, 8, 8)
        }
        // Top Left corner
        this.drawTexture(x, y, 0, 0, 8, 8)
        // Top right corner
        this.drawTexture(lastXTile, y, 8, 0, 8 ,8)
        // Bottom right corner
        this.drawTexture(lastXTile, lastYTile, 8, 8, 8 ,8)
        // Bottom left corner
        this.drawTexture(x, lastYTile, 0, 8, 8, 8)

        // Slots
        val slotStartX = x + (this.backgroundWidth - (18 * chest.material.columns)) / 2
        val slotStartY = 8+y

        for (row in 0 until chest.material.rows) {
            for (column in 0 until chest.material.columns) {
                drawTexture(slotStartX + (18 * column), slotStartY + (18 * row), 16, 0, 18, 18)
            }
        }

        // Inventory
        val inventoryStartX = x + ((this.backgroundWidth - 162) / 2)
        val inventoryStartY = y + this.backgroundHeight - 84

        for (row in 0 until 3) {
            for (column in 0 until 9) {
                drawTexture(inventoryStartX + (18 * column), inventoryStartY + (18 * row), 16, 0, 18, 18)
            }
        }

        val hotbarStartY = inventoryStartY + (3*18) + 4
        for (column in 0 until 9) {
            drawTexture(inventoryStartX + (18 * column), hotbarStartY, 16, 0, 18, 18)
        }
    }

}

