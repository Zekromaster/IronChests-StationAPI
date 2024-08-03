package net.zekromaster.minecraft.ironchests

import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.nbt.NbtCompound
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent
import net.zekromaster.minecraft.ironchests.mixin.ChestInventoryAccessor

internal object IronChestsBlockEntityEntrypoint {
    @EventListener
    internal fun registerTileEntities(event: BlockEntityRegisterEvent) {
        event.register(
            IronChestBlockEntity::class.java,
            "ironchest"
        )
    }
}

class IronChestBlockEntity @JvmOverloads constructor(material: IronChestMaterial = IronChestMaterial.IRON): ChestBlockEntity() {
    var material: IronChestMaterial = material
        set(x) {
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
