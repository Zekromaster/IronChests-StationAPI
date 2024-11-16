package net.zekromaster.minecraft.ironchests.upgrades

import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.block.Block
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.modificationstation.stationapi.api.client.item.CustomTooltipProvider
import net.modificationstation.stationapi.api.event.recipe.RecipeRegisterEvent
import net.modificationstation.stationapi.api.event.registry.ItemRegistryEvent
import net.modificationstation.stationapi.api.recipe.CraftingRegistry
import net.modificationstation.stationapi.api.registry.ItemRegistry
import net.modificationstation.stationapi.api.tag.TagKey
import net.modificationstation.stationapi.api.util.Identifier
import net.modificationstation.stationapi.api.util.math.Direction
import net.zekromaster.minecraft.ironchests.IronChestsBlockStates.FACING
import net.zekromaster.minecraft.ironchests.IronChestsBlockStates.HAS_OBSIDIAN_UPGRADE
import net.zekromaster.minecraft.terminal.capabilities.BlockCapability
import net.zekromaster.minecraft.terminal.capabilities.CapabilityEvents
import net.zekromaster.minecraft.ironchests.*
import net.zekromaster.minecraft.ironchests.IronChestMaterial.Companion.DIAMOND
import net.zekromaster.minecraft.ironchests.IronChestMaterial.Companion.GOLD
import net.zekromaster.minecraft.ironchests.IronChestMaterial.Companion.IRON


interface Tiered {

    val currentTier: IronChestMaterial?

    fun switchTier(destination: IronChestMaterial, player: PlayerEntity?): Boolean

    companion object {
        @JvmStatic @get:JvmName("capability")
        val CAPABILITY: BlockCapability<Tiered, Void> = BlockCapability.createVoid(
            Identifier.of("ironchests:tiered"),
            Tiered::class.java
        )
    }
}

private class VanillaChestTieredImpl(val chest: ChestBlockEntity) : Tiered {
    override val currentTier: IronChestMaterial? = null

    override fun switchTier(destination: IronChestMaterial, player: PlayerEntity?): Boolean {
        val world = chest.world
        val x = chest.x
        val y = chest.y
        val z = chest.z

        val oldInventory = chest.inventory().copyOf(destination.size)

        world.setBlockStateWithNotify(
            x, y, z,
            destination.getBlock().defaultState.with(FACING, player?.placementFacing() ?: Direction.NORTH)
        )
        (world.getBlockEntity(x, y, z) as IronChestBlockEntity).inventory(oldInventory)
        world.setBlockDirty(x, y, z)
        return true
    }
}

private class IronChestTieredImpl(val chest: IronChestBlockEntity) : Tiered {
    override val currentTier: IronChestMaterial
        get() = chest.material

    override fun switchTier(destination: IronChestMaterial, player: PlayerEntity?): Boolean {
        val world = chest.world
        val x = chest.x
        val y = chest.y
        val z = chest.z

        chest.material = destination

        val originalBlockState = world.getBlockState(x, y, z)

        world.setBlockState(
            x, y, z,
            destination.getBlock().defaultState
                .with(FACING, originalBlockState.get(FACING) ?: Direction.NORTH)
                .with(HAS_OBSIDIAN_UPGRADE, originalBlockState.get(HAS_OBSIDIAN_UPGRADE) ?: false)
        )
        world.setBlockEntity(x, y, z, chest)
        world.setBlockDirty(x, y, z)
        chest.markDirty()
        return true
    }
}

private class TierUpgrade(identifier: Identifier, val starting: IronChestMaterial?, val destination: IronChestMaterial):
    ChestUpgrade(identifier), CustomTooltipProvider
{

    override fun innerUpgrade(world: World, x: Int, y: Int, z: Int, player: PlayerEntity, blockEntity: ChestBlockEntity): Boolean {
        val tiered = Tiered.CAPABILITY.get(world, x, y, z, null)
        if (tiered == null || tiered.currentTier != starting) {
            return false
        }

        return tiered.switchTier(destination, player)
    }

    override fun getTooltip(stack: ItemStack, originalTooltip: String) = arrayOf(
        originalTooltip,
        "${starting?.displayName ?: "Wood"} to ${destination.displayName}"
    )

}

object TierUpgrades {

    @JvmStatic @get:JvmName("woodToIron")
    lateinit var WOOD_TO_IRON: ChestUpgrade
        private set
    @JvmStatic @get:JvmName("ironToGold")
    lateinit var IRON_TO_GOLD: ChestUpgrade
        private set
    @JvmStatic @get:JvmName("goldToDiamond")
    lateinit var GOLD_TO_DIAMOND: ChestUpgrade
        private set

    @EventListener
    private fun registerCapabilities(event: CapabilityEvents.RegisterBlockEntityCapabilitiesEvent) {
        event.register(
            Tiered.CAPABILITY,
            { c, _ -> VanillaChestTieredImpl(c as ChestBlockEntity) },
            "Chest"
        )
        event.register(
            Tiered.CAPABILITY,
            { c, _ -> IronChestTieredImpl(c as IronChestBlockEntity) },
            "IronChest"
        )
    }

    @EventListener
    private fun registerItems(event: ItemRegistryEvent) {
        Identifier.of("ironchests:upgrades/wood_to_iron").apply {
            WOOD_TO_IRON = TierUpgrade(this, null, IRON)
            WOOD_TO_IRON.setTranslationKey(this)
        }
        Identifier.of("ironchests:upgrades/iron_to_gold").apply {
            IRON_TO_GOLD = TierUpgrade(this, IRON, GOLD)
            IRON_TO_GOLD.setTranslationKey(this)
        }
        Identifier.of("ironchests:upgrades/gold_to_diamond").apply {
            GOLD_TO_DIAMOND = TierUpgrade(this, GOLD, DIAMOND)
            GOLD_TO_DIAMOND.setTranslationKey(this)
        }
    }

    @EventListener
    private fun registerRecipes(event: RecipeRegisterEvent) {
        if (RecipeRegisterEvent.Vanilla.fromType(event.recipeId) != RecipeRegisterEvent.Vanilla.CRAFTING_SHAPED) {
            return
        }

        CraftingRegistry.addShapedRecipe(
            ItemStack(WOOD_TO_IRON),
            "iii", "ici", "iii",
            'i', ItemStack(Item.IRON_INGOT),
            'c', TagKey.of(ItemRegistry.KEY, Identifier.of("planks"))
        )
        CraftingRegistry.addShapedRecipe(
            ItemStack(IRON_TO_GOLD),
            "iii", "ici", "iii",
            'i', ItemStack(Item.GOLD_INGOT),
            'c', ItemStack(Item.IRON_INGOT)
        )
        CraftingRegistry.addShapedRecipe(
            ItemStack(GOLD_TO_DIAMOND),
            "gig", "ici", "gig",
            'i', ItemStack(Item.DIAMOND),
            'c', ItemStack(Item.GOLD_INGOT),
            'g', ItemStack(Block.GLASS)
        )
        CraftingRegistry.addShapedRecipe(
            ItemStack(GOLD_TO_DIAMOND),
            "igi", "gcg", "igi",
            'i', ItemStack(Item.DIAMOND),
            'c', ItemStack(Item.GOLD_INGOT),
            'g', ItemStack(Block.GLASS)
        )
    }
}