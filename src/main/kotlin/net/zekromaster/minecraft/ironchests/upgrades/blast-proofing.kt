package net.zekromaster.minecraft.ironchests.upgrades

import net.mine_diver.unsafeevents.listener.EventListener
import net.minecraft.block.Block
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.modificationstation.stationapi.api.client.item.CustomTooltipProvider
import net.modificationstation.stationapi.api.event.recipe.RecipeRegisterEvent
import net.modificationstation.stationapi.api.event.registry.ItemRegistryEvent
import net.modificationstation.stationapi.api.recipe.CraftingRegistry
import net.modificationstation.stationapi.api.registry.ItemRegistry
import net.modificationstation.stationapi.api.tag.TagKey
import net.modificationstation.stationapi.api.util.Identifier
import net.zekromaster.minecraft.ironchests.IronChestsBlockEntrypoint
import net.zekromaster.minecraft.ironchests.IronChestsBlockStates.HAS_OBSIDIAN_UPGRADE
import net.zekromaster.minecraft.terminal.capabilities.CapabilityEvents
import net.zekromaster.minecraft.terminal.utils.DynamicBlastProof

private class ObsidianUpgrade(identifier: Identifier): ChestUpgrade(identifier), CustomTooltipProvider {
    override fun innerUpgrade(world: World, x: Int, y: Int, z: Int, player: PlayerEntity, blockEntity: ChestBlockEntity): Boolean {
        val blastProofable = DynamicBlastProof.CAPABILITY.get(world, x, y, z, null)
        if (blastProofable?.isBlastProof != false) {
            return false
        }
        blastProofable.isBlastProof = true
        UpgradeStorage.CAPABILITY.get(world, x, y, z, null)?.add(ItemStack(this))
        return true
    }

    override fun getTooltip(stack: ItemStack, originalTooltip: String) = arrayOf(originalTooltip, "Makes any non-Wooden Chest blast resistant")
}

@EventListener
internal object BlastProofUpgrade {

    @JvmStatic @get:JvmName("blastProofUpgrade")
    lateinit var BLAST_PROOF_UPGRADE: ChestUpgrade
        private set


    @EventListener
    private fun registerCapability(event: CapabilityEvents.RegisterBlockCapabilitiesEvent) {
        event.register(
            DynamicBlastProof.CAPABILITY,
            DynamicBlastProof.blockState(HAS_OBSIDIAN_UPGRADE),
            IronChestsBlockEntrypoint.IRON_CHEST,
            IronChestsBlockEntrypoint.GOLD_CHEST,
            IronChestsBlockEntrypoint.DIAMOND_CHEST
        )
    }

    @EventListener
    fun registerItems(event: ItemRegistryEvent) {
        Identifier.of("ironchests:upgrades/obsidian").apply {
            BLAST_PROOF_UPGRADE = ObsidianUpgrade(this)
            BLAST_PROOF_UPGRADE.setTranslationKey(this)
        }
    }

    @EventListener
    internal fun registerRecipes(event: RecipeRegisterEvent) {
        val type = RecipeRegisterEvent.Vanilla.fromType(event.recipeId)

        if (type == RecipeRegisterEvent.Vanilla.CRAFTING_SHAPED) {

            CraftingRegistry.addShapedRecipe(
                ItemStack(BLAST_PROOF_UPGRADE),
                "ooo", "oco", "ooo",
                'o', ItemStack(Block.OBSIDIAN),
                'c', TagKey.of(ItemRegistry.KEY, Identifier.of("planks"))
            )
        }
    }
}

