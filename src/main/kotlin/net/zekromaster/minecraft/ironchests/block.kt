package net.zekromaster.minecraft.ironchests

import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.material.Material
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.world.World
import net.modificationstation.stationapi.api.block.BlockState
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent
import net.modificationstation.stationapi.api.event.recipe.RecipeRegisterEvent
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent
import net.modificationstation.stationapi.api.gui.screen.container.GuiHelper
import net.modificationstation.stationapi.api.item.ItemPlacementContext
import net.modificationstation.stationapi.api.recipe.CraftingRegistry
import net.modificationstation.stationapi.api.state.StateManager
import net.modificationstation.stationapi.api.state.property.EnumProperty
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity
import net.modificationstation.stationapi.api.util.Identifier
import net.modificationstation.stationapi.api.util.math.Direction
import net.zekromaster.minecraft.ironchests.mixin.ChestInventoryAccessor
import java.util.*
import java.util.function.Predicate


object IronChestsBlockEntrypoint {
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
    fun registerBlocks(event: BlockRegistryEvent) {
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

class IronChestBlockEntity @JvmOverloads constructor(material: IronChestMaterial = IronChestMaterial.IRON): ChestBlockEntity() {
    var material = material
        set(x) = run {
            field = x
            updateInventorySize()
        }

    override fun size(): Int = material.size
    override fun getName(): String = material.chestName

    init {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as ChestInventoryAccessor).inventory = arrayOfNulls(material.size)
    }

    override fun readNbt(nbt: NbtCompound) {
        material = IronChestMaterial.from(nbt.getString("Material").ifBlank { "iron" })
        super.readNbt(nbt)
    }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putString("Material", material.id)
        super.writeNbt(nbt)
    }

    private fun updateInventorySize() {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as ChestInventoryAccessor).inventory = inventory.copyOf(material.size)
    }
}

class IronChestBlock(identifier: Identifier, private val chestMaterial: IronChestMaterial): TemplateBlockWithEntity(identifier, Material.METAL) {
    init {
        setHardness(5.0F)
        setResistance(10.0F)
        setSoundGroup(METAL_SOUND_GROUP)
        defaultState = defaultState.with(FACING, Direction.NORTH)
    }

    private val random = Random()

    companion object {
        @JvmField
        val FACING: EnumProperty<Direction> = EnumProperty.of("facing", Direction::class.java, Predicate { it.axis.isHorizontal })
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
        super.appendProperties(builder)
    }

    override fun getPlacementState(context: ItemPlacementContext): BlockState = defaultState.with(FACING, context.player!!.placementFacing())

    override fun createBlockEntity(): BlockEntity {
        return IronChestBlockEntity(chestMaterial)
    }

    override fun onBreak(world: World, x: Int, y: Int, z: Int) {
        val entity = world.getBlockEntity(x, y, z) as IronChestBlockEntity

        for (index in 0 until entity.size()) {
            val item = entity.getStack(index) ?: continue
            val dropX: Float = this.random.nextFloat() * 0.8f + 0.1f
            val dropY: Float = this.random.nextFloat() * 0.8f + 0.1f
            val dropZ: Float = this.random.nextFloat() * 0.8f + 0.1f

            while (item.count > 0) {
                var dropAmount: Int = this.random.nextInt(21) + 10
                if (dropAmount > item.count) {
                    dropAmount = item.count
                }

                item.count -= dropAmount
                val drop = ItemEntity(
                    world,
                    (x.toFloat() + dropX).toDouble(),
                    (y.toFloat() + dropY).toDouble(),
                    (z.toFloat() + dropZ).toDouble(), ItemStack(item.itemId, dropAmount, item.damage)
                )
                val velocityScale = 0.05f
                drop.velocityX = (this.random.nextGaussian().toFloat() * velocityScale).toDouble()
                drop.velocityY = (this.random.nextGaussian().toFloat() * velocityScale + 0.2f).toDouble()
                drop.velocityZ = (this.random.nextGaussian().toFloat() * velocityScale).toDouble()
                world.spawnEntity(drop)
            }

        }
        super.onBreak(world, x, y, z)
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

        GuiHelper.openGUI(player, Identifier.of("ironchests:gui_${chestMaterial.id}"), entity, GenericContainerScreenHandler(player.inventory, entity))
        return true
    }
}