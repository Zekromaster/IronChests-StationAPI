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

internal object IronChestsGUIEntrypoint {

    private class OpenInventory(private val material: IronChestMaterial): BiFunction<PlayerEntity, Inventory, Screen> {
        override fun apply(player: PlayerEntity, inventory: Inventory): Screen =
            IronChestScreen(player.inventory, inventory, material)
    }

    private data class IronChestFactory(val material: IronChestMaterial): Supplier<Inventory> {
        override fun get(): IronChestBlockEntity = IronChestBlockEntity(material)
    }

    @EventListener
    fun registerGUIs(event: GuiHandlerRegistryEvent) {
        event.registry.registerValueNoMessage(
            Identifier.of("ironchests:gui_iron"), BiTuple.of(
            OpenInventory(IronChestMaterial.IRON),
            IronChestFactory(IronChestMaterial.IRON)
        ))
        event.registry.registerValueNoMessage(
            Identifier.of("ironchests:gui_gold"), BiTuple.of(
            OpenInventory(IronChestMaterial.GOLD),
            IronChestFactory(IronChestMaterial.GOLD)
        ))
        event.registry.registerValueNoMessage(
            Identifier.of("ironchests:gui_diamond"), BiTuple.of(
            OpenInventory(IronChestMaterial.DIAMOND),
            IronChestFactory(IronChestMaterial.DIAMOND)
        ))
    }
}

private class IronChestScreenHandler(
    material: IronChestMaterial,
    playerInventory: Inventory,
    val chestInventory: Inventory,
): ScreenHandler() {
    val screenWidth = 24 + (material.columns * 18)
    val screenHeight = 16 + (material.rows * 18) + 80

    init {
        for (row in 0 until material.rows) {
            for (column in 0 until material.columns) {
                addSlot(Slot(chestInventory, column + (row * material.columns), 13 + (column * 18), 9 + (row * 18)))
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
        chestInventory.canPlayerUse(player)
}

@Environment(EnvType.CLIENT)
private class IronChestScreen(
    playerInventory: Inventory,
    inventory: Inventory,
    private val material: IronChestMaterial
) : HandledScreen(IronChestScreenHandler(material, playerInventory, inventory)) {
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

        // Top left corner
        val topLeft = minecraft.textureManager.getTextureId("/assets/ironchests/stationapi/textures/gui/chest.png")
        minecraft.textureManager.bindTexture(topLeft)

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
        val slotStartX = 12+x
        val slotStartY = 8+y

        for (row in 0 until material.rows) {
            for (column in 0 until material.columns) {
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

