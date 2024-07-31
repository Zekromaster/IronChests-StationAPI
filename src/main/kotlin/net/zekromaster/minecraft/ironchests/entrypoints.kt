package net.zekromaster.minecraft.ironchests

import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.block.Block
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent
import net.modificationstation.stationapi.api.event.recipe.RecipeRegisterEvent
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent
import net.modificationstation.stationapi.api.event.registry.GuiHandlerRegistryEvent
import net.modificationstation.stationapi.api.recipe.CraftingRegistry
import net.modificationstation.stationapi.api.util.Identifier
import uk.co.benjiweber.expressions.tuple.BiTuple
import java.util.function.BiFunction
import java.util.function.Supplier

object IronChests {

    @JvmStatic @get:JvmName("ironChest")
    lateinit var IRON_CHEST: IronChestBlock
        private set
    @JvmStatic @get:JvmName("goldChest")
    lateinit var GOLD_CHEST: IronChestBlock
        private set
    @JvmStatic @get:JvmName("diamondChest")
    lateinit var DIAMOND_CHEST: IronChestBlock
        private set

    @EventListener
    internal fun registerBlocks(event: BlockRegistryEvent) {
        IRON_CHEST = IronChestBlock(Identifier.of("ironchests:iron_chest"), IronChestMaterial.IRON)
        IRON_CHEST.setTranslationKey(Identifier.of("ironchests:iron_chest"))
        GOLD_CHEST = IronChestBlock(Identifier.of("ironchests:gold_chest"), IronChestMaterial.GOLD)
        GOLD_CHEST.setTranslationKey(Identifier.of("ironchests:gold_chest"))
        DIAMOND_CHEST = IronChestBlock(Identifier.of("ironchests:diamond_chest"), IronChestMaterial.DIAMOND)
        DIAMOND_CHEST.setTranslationKey(Identifier.of("ironchests:diamond_chest"))
    }

    @EventListener
    internal fun registerTileEntities(event: BlockEntityRegisterEvent) {
        event.register(
            IronChestBlockEntity::class.java,
            "ironchest"
        )
    }

    @EventListener
    internal fun registerRecipes(event: RecipeRegisterEvent) {
        val type = RecipeRegisterEvent.Vanilla.fromType(event.recipeId)

        if (type == RecipeRegisterEvent.Vanilla.CRAFTING_SHAPED) {
            CraftingRegistry.addShapedRecipe(
                ItemStack(IRON_CHEST),
                "iii", "ici", "iii",
                'i', ItemStack(Item.IRON_INGOT),
                'c', ItemStack(Block.CHEST)
            )
            CraftingRegistry.addShapedRecipe(
                ItemStack(GOLD_CHEST),
                "iii", "ici", "iii",
                'i', ItemStack(Item.GOLD_INGOT),
                'c', ItemStack(IRON_CHEST)
            )
            CraftingRegistry.addShapedRecipe(
                ItemStack(DIAMOND_CHEST),
                "gig", "ici", "gig",
                'i', ItemStack(Item.DIAMOND),
                'c', ItemStack(GOLD_CHEST),
                'g', ItemStack(Block.GLASS)
            )
            CraftingRegistry.addShapedRecipe(
                ItemStack(DIAMOND_CHEST),
                "igi", "gcg", "igi",
                'i', ItemStack(Item.DIAMOND),
                'c', ItemStack(GOLD_CHEST),
                'g', ItemStack(Block.GLASS)
            )
        }
    }

}


internal object RegisterGUIs {

    private class OpenInventory(private val material: IronChestMaterial): BiFunction<PlayerEntity, Inventory, Screen> {
        override fun apply(player: PlayerEntity, inventory: Inventory): Screen =
            IronChestScreen(player.inventory, inventory, material)
    }

    private data class IronChestFactory(val material: IronChestMaterial): Supplier<Inventory> {
        override fun get(): IronChestBlockEntity = IronChestBlockEntity(material)
    }

    @EventListener
    fun registerGUIs(event: GuiHandlerRegistryEvent) {
        event.registry.registerValueNoMessage(Identifier.of("ironchests:gui_iron"), BiTuple.of(
            OpenInventory(IronChestMaterial.IRON),
            IronChestFactory(IronChestMaterial.IRON)
        ))
        event.registry.registerValueNoMessage(Identifier.of("ironchests:gui_gold"), BiTuple.of(
            OpenInventory(IronChestMaterial.GOLD),
            IronChestFactory(IronChestMaterial.GOLD)
        ))
        event.registry.registerValueNoMessage(Identifier.of("ironchests:gui_diamond"), BiTuple.of(
            OpenInventory(IronChestMaterial.DIAMOND),
            IronChestFactory(IronChestMaterial.DIAMOND)
        ))
    }
}