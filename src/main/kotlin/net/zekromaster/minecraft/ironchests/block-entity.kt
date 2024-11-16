package net.zekromaster.minecraft.ironchests

import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent
import net.zekromaster.minecraft.ironchests.IronChestMaterial.Companion.IRON

internal object IronChestsBlockEntityEntrypoint {
    @EventListener
    internal fun registerTileEntities(event: BlockEntityRegisterEvent) {
        event.register(
            IronChestBlockEntity::class.java,
            "IronChest"
        )
    }
}

class IronChestBlockEntity @JvmOverloads constructor(material: IronChestMaterial = IRON): ChestBlockEntity() {
    var material: IronChestMaterial = material
        set(x) {
            field = x
            updateInventorySize()
        }

    val rows: Int
        get() = material.rows
    val columns: Int
        get() = material.columns

    override fun size(): Int = material.size
    override fun getName(): String = material.chestName

    internal val storedUpgrades: MutableList<ItemStack> = mutableListOf()

    init {
        inventory(arrayOfNulls(material.size))
    }

    override fun readNbt(nbt: NbtCompound) {
        material = nbt.getString("Material")?.let { IronChestMaterial.from(it) } ?: IRON
        super.readNbt(nbt)
    }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putString("Material", material.id.toString())
        super.writeNbt(nbt)
    }

    private fun updateInventorySize() {
        this.inventory(this.inventory().copyOf(material.size))
    }
}
