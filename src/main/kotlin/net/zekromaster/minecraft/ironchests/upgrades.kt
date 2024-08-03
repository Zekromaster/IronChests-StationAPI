package net.zekromaster.minecraft.ironchests

import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.block.Block
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.modificationstation.stationapi.api.event.recipe.RecipeRegisterEvent
import net.modificationstation.stationapi.api.event.registry.ItemRegistryEvent
import net.modificationstation.stationapi.api.recipe.CraftingRegistry
import net.modificationstation.stationapi.api.registry.ItemRegistry
import net.modificationstation.stationapi.api.tag.TagKey
import net.modificationstation.stationapi.api.template.item.TemplateItem
import net.modificationstation.stationapi.api.util.Identifier
import net.modificationstation.stationapi.api.util.math.Direction
import net.zekromaster.minecraft.ironchests.IronChestsBlockStates.FACING
import net.zekromaster.minecraft.ironchests.IronChestMaterial.*
import net.zekromaster.minecraft.ironchests.mixin.ChestInventoryAccessor

internal object IronChestsUpgradesEntrypoint {

    @JvmStatic @get:JvmName("woodToIron")
    lateinit var WOOD_TO_IRON: ChestUpgrade
        private set
    @JvmStatic @get:JvmName("ironToGold")
    lateinit var IRON_TO_GOLD: ChestUpgrade
        private set
    @JvmStatic @get:JvmName("goldToDiamond")
    lateinit var GOLD_TO_DIAMOND: ChestUpgrade
        private set
    @JvmStatic @get:JvmName("obsidian")
    lateinit var OBSIDIAN: ChestUpgrade
        private set

    @EventListener
    fun registerItems(event: ItemRegistryEvent) {
        Identifier.of("ironchests:upgrades/wood_to_iron").apply {
            WOOD_TO_IRON = WoodToIronUpgrade(this, IRON)
            WOOD_TO_IRON.setTranslationKey(this)
        }
        Identifier.of("ironchests:upgrades/iron_to_gold").apply {
            IRON_TO_GOLD = IronToIronUpgrade(this, IRON, GOLD)
            IRON_TO_GOLD.setTranslationKey(this)
        }
        Identifier.of("ironchests:upgrades/gold_to_diamond").apply {
            GOLD_TO_DIAMOND = IronToIronUpgrade(this, GOLD, DIAMOND)
            GOLD_TO_DIAMOND.setTranslationKey(this)
        }
        Identifier.of("ironchests:upgrades/obsidian").apply {
            OBSIDIAN = ObsidianUpgrade(this)
            OBSIDIAN.setTranslationKey(this)
        }
    }

    @EventListener
    internal fun registerRecipes(event: RecipeRegisterEvent) {
        val type = RecipeRegisterEvent.Vanilla.fromType(event.recipeId)

        if (type == RecipeRegisterEvent.Vanilla.CRAFTING_SHAPED) {
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
            CraftingRegistry.addShapedRecipe(
                ItemStack(OBSIDIAN),
                "ooo", "oco", "ooo",
                'o', ItemStack(Block.OBSIDIAN),
                'c', TagKey.of(ItemRegistry.KEY, Identifier.of("planks"))
            )
        }
    }
}

abstract class ChestUpgrade(identifier: Identifier): TemplateItem(identifier) {
    abstract fun upgrade(world: World, x: Int, y: Int, z: Int, player: PlayerEntity, blockEntity: ChestBlockEntity): Boolean
}

private sealed class TierUpgrade(identifier: Identifier, private val destination: IronChestMaterial): ChestUpgrade(identifier) {
    override fun upgrade(world: World, x: Int, y: Int, z: Int, player: PlayerEntity, blockEntity: ChestBlockEntity): Boolean {
        if (canUpgrade(blockEntity)) {
            val oldBlockState = world.getBlockState(x, y, z)
            val oldContents = (blockEntity as ChestInventoryAccessor).inventory.copyOf()
            val oldBlock = blockEntity.block

            (blockEntity as ChestInventoryAccessor).inventory = arrayOfNulls(blockEntity.size())
            world.setBlock(x, y, z, destination.getBlock().id)

            if (oldBlock is IronChestBlock) {
                world.setBlockState(x, y, z, world.getBlockState(x, y, z).with(FACING, oldBlockState.get(FACING) ?: Direction.NORTH))
            } else {
                world.setBlockState(x, y, z, world.getBlockState(x, y, z).with(FACING, player.placementFacing()))
            }

            val newEntity = world.getBlockEntity(x, y, z) as IronChestBlockEntity
            @Suppress("CAST_NEVER_SUCCEEDS")
            (newEntity as ChestInventoryAccessor).inventory = oldContents.copyOf(newEntity.size())
            world.setBlockDirty(x, y, z)
            blockEntity.markDirty()
            return true
        }
        return false
    }

    protected abstract fun canUpgrade(blockEntity: ChestBlockEntity): Boolean
}

private class WoodToIronUpgrade(identifier: Identifier, destination: IronChestMaterial): TierUpgrade(identifier, destination) {
    override fun canUpgrade(blockEntity: ChestBlockEntity) = blockEntity.block == Block.CHEST
}

private class IronToIronUpgrade(identifier: Identifier, val starting: IronChestMaterial, destination: IronChestMaterial): TierUpgrade(identifier, destination) {
    override fun canUpgrade(blockEntity: ChestBlockEntity): Boolean = blockEntity is IronChestBlockEntity && blockEntity.material == starting
}

private class ObsidianUpgrade(identifier: Identifier): ChestUpgrade(identifier) {
    override fun upgrade(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        player: PlayerEntity,
        blockEntity: ChestBlockEntity
    ): Boolean {
        val blockState = world.getBlockState(x, y, z)
        if (blockState.block !is IronChestBlock || blockState.get(IronChestsBlockStates.HAS_OBSIDIAN_UPGRADE)) {
            return false
        }

        val entity = world.getBlockEntity(x, y, z) as IronChestBlockEntity
        world.setBlockStateWithNotify(x, y, z, blockState.with(IronChestsBlockStates.HAS_OBSIDIAN_UPGRADE, true))
        world.setBlockEntity(x, y, z, entity)
        entity.isBlastResistant = true
        world.setBlockDirty(x, y, z)
        return true
    }
}