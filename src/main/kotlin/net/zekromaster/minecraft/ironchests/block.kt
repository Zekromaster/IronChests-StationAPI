package net.zekromaster.minecraft.ironchests

import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.material.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.modificationstation.stationapi.api.block.BlockState
import net.modificationstation.stationapi.api.event.recipe.RecipeRegisterEvent
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent
import net.modificationstation.stationapi.api.gui.screen.container.GuiHelper
import net.modificationstation.stationapi.api.item.ItemPlacementContext
import net.modificationstation.stationapi.api.recipe.CraftingRegistry
import net.modificationstation.stationapi.api.state.StateManager
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity
import net.modificationstation.stationapi.api.util.Identifier
import net.modificationstation.stationapi.api.util.math.Direction
import net.zekromaster.minecraft.ironchests.IronChestsBlockStates.FACING
import net.zekromaster.minecraft.ironchests.IronChestsBlockStates.HAS_OBSIDIAN_UPGRADE
import net.zekromaster.minecraft.ironchests.upgrades.ChestUpgrade

internal object IronChestsBlockEntrypoint {
    @JvmStatic
    lateinit var IRON_CHEST: IronChestBlock
        private set
    @JvmStatic
    lateinit var GOLD_CHEST: IronChestBlock
        private set
    @JvmStatic
    lateinit var DIAMOND_CHEST: IronChestBlock
        private set

    @EventListener
    fun registerBlocks(event: BlockRegistryEvent) {
        IRON_CHEST = IronChestBlock(Identifier.of("ironchests:iron_chest"), IronChestMaterial.IRON)
        IRON_CHEST.setTranslationKey(Identifier.of("ironchests:iron_chest"))
        GOLD_CHEST = IronChestBlock(Identifier.of("ironchests:gold_chest"), IronChestMaterial.GOLD)
        GOLD_CHEST.setTranslationKey(Identifier.of("ironchests:gold_chest"))
        DIAMOND_CHEST = IronChestBlock(Identifier.of("ironchests:diamond_chest"), IronChestMaterial.DIAMOND)
        DIAMOND_CHEST.setTranslationKey(Identifier.of("ironchests:diamond_chest"))
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


class IronChestBlock(identifier: Identifier, private val chestMaterial: IronChestMaterial): TemplateBlockWithEntity(identifier, Material.METAL) {
    init {
        setHardness(5.0F)
        setResistance(10.0F)
        setSoundGroup(METAL_SOUND_GROUP)
        defaultState = defaultState.with(FACING, Direction.NORTH).with(HAS_OBSIDIAN_UPGRADE, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING, HAS_OBSIDIAN_UPGRADE)
        super.appendProperties(builder)
    }

    override fun getPlacementState(context: ItemPlacementContext): BlockState =
        defaultState.with(FACING, context.player!!.placementFacing()).with(HAS_OBSIDIAN_UPGRADE, false)

    override fun createBlockEntity(): BlockEntity = chestMaterial.createBlockEntity()

    override fun onBreak(world: World?, x: Int, y: Int, z: Int) {
        Block.CHEST.onBreak(world, x, y, z)
    }

    override fun onUse(world: World, x: Int, y: Int, z: Int, player: PlayerEntity): Boolean {
        val entity = world.getBlockEntity(x, y, z) ?: return true

        if (entity !is IronChestBlockEntity || world.isRemote) {
            return true
        }

        val handheldItem = player.hand?.item
        if (handheldItem is ChestUpgrade) {
            if (handheldItem.upgrade(world, x, y, z, player, entity)) {
                player.hand!!.count--
            }
            return true
        }

        if (world.shouldSuffocate(x, y+1, z)) {
            return true
        }

        GuiHelper.openGUI(player, Identifier.of("ironchests:iron_chest"), entity, IronChestScreenHandler(player.inventory, entity))
        return true
    }
}