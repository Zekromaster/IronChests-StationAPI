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
}

private class IronChestScreenHandler(
    type: GUIType,
    playerInventory: Inventory,
    val chestInventory: Inventory,
    screenWidth: Int,
    screenHeight: Int
): ScreenHandler() {
    init {
        for (row in 0 until type.material.rows) {
            for (column in 0 until type.material.columns) {
                addSlot(Slot(chestInventory, column + (row * type.material.columns), 12 + (column * 18), 8 + (row * 18)))
            }
        }

        playerInventory.draw(xOffset = ((screenWidth - 162) / 2) + 1, screenHeight = screenHeight)
    }

    private fun Inventory.draw(xOffset: Int, screenHeight: Int) {
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                addSlot(Slot(this, col + row * 9 + 9, xOffset + col * 18, screenHeight - (4 - row) * 18 - 10))
            }
        }

        for (hotbarSlot in 0 until 9) {
            addSlot(Slot(this, hotbarSlot,xOffset + hotbarSlot * 18, screenHeight - 24))
        }
    }


    override fun canUse(player: PlayerEntity): Boolean =
        chestInventory.canPlayerUse(player)
}

@Environment(EnvType.CLIENT)
private class IronChestScreen(
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
