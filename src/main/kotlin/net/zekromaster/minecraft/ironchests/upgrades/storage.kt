package net.zekromaster.minecraft.ironchests.upgrades

import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.item.ItemStack
import net.modificationstation.stationapi.api.util.Identifier
import net.zekromaster.minecraft.ironchests.IronChestBlockEntity
import net.zekromaster.minecraft.terminal.capabilities.BlockCapability
import net.zekromaster.minecraft.terminal.capabilities.CapabilityEvents
import net.zekromaster.minecraft.terminal.storage.ItemStorage

interface UpgradeStorage {
    val storage: Collection<ItemStack>

    fun add(stack: ItemStack)

    companion object {
        @JvmStatic @get:JvmName("capability")
        val CAPABILITY: BlockCapability<UpgradeStorage, Void> = BlockCapability.createVoid(
            Identifier.of("ironchests:upgrade_storage"),
            UpgradeStorage::class.java
        )
    }
}

private class UpgradeStorageImpl(val chest: IronChestBlockEntity): UpgradeStorage {
    override val storage: Collection<ItemStack>
        get() = chest.storedUpgrades

    override fun add(stack: ItemStack) {
        chest.storedUpgrades.add(stack)
    }
}

internal object UpgradeStorageEntrypoint {

    @EventListener
    private fun registerCapabilities(event: CapabilityEvents.RegisterBlockEntityCapabilitiesEvent) {
        event.register(
            UpgradeStorage.CAPABILITY,
            { c, _ -> UpgradeStorageImpl(c as IronChestBlockEntity) },
            "IronChest"
        )

        event.register(
            ItemStorage.BLOCK,
            { c, _ -> ItemStorage.of(c as IronChestBlockEntity) },
            "IronChest"
        )
    }

}