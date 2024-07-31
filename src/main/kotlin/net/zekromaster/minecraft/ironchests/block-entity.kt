package net.zekromaster.minecraft.ironchests

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.material.Material
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.world.World
import net.modificationstation.stationapi.api.block.BlockState
import net.modificationstation.stationapi.api.gui.screen.container.GuiHelper
import net.modificationstation.stationapi.api.item.ItemPlacementContext
import net.modificationstation.stationapi.api.state.StateManager
import net.modificationstation.stationapi.api.state.property.EnumProperty
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity
import net.modificationstation.stationapi.api.util.Identifier
import net.modificationstation.stationapi.api.util.math.Direction
import net.zekromaster.minecraft.ironchests.mixin.ChestInventoryAccessor
import java.util.*
import java.util.function.Predicate
import kotlin.math.floor


class IronChestBlockEntity(material: IronChestMaterial): ChestBlockEntity() {
     constructor(): this(IronChestMaterial.IRON)

    private var material = material
        set(x) = run {
            field = x
            updateInventorySize()
        }

    override fun size(): Int = material.size
    override fun getName(): String = material.chestName

    init {
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
        (this as ChestInventoryAccessor).inventory.copyOf(material.size)
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

    override fun getPlacementState(context: ItemPlacementContext): BlockState {
        val direction: Int = floor((context.player!!.yaw * 4.0f / 360.0f).toDouble() + 0.5).toInt() and 3
        return when (direction) {
            0 -> defaultState.with(FACING, Direction.NORTH)
            1 -> defaultState.with(FACING, Direction.EAST)
            2 -> defaultState.with(FACING, Direction.SOUTH)
            3 -> defaultState.with(FACING, Direction.WEST)
            else -> defaultState.with(FACING, Direction.NORTH)
        }
    }

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
        if (entity !is IronChestBlockEntity || world.shouldSuffocate(x, y+1, z) || world.isRemote) {
            return true
        }

        GuiHelper.openGUI(player, Identifier.of("ironchests:gui_${chestMaterial.id}"), entity, GenericContainerScreenHandler(player.inventory, entity))
        return true
    }
}