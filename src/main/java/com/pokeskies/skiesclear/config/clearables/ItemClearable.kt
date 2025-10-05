package com.pokeskies.skiesclear.config.clearables

import com.pokeskies.skiesclear.config.ClearConfig
import com.pokeskies.skiesclear.utils.Utils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.entity.EntityTypeTest

class ItemClearable(
    enabled: Boolean = false,
    blacklist: List<String> = emptyList(),
    whitelist: List<String> = emptyList()
): Clearable<ItemEntity>(enabled, blacklist, whitelist) {
    override fun getResourceLocation(entity: ItemEntity): ResourceLocation {
        return BuiltInRegistries.ITEM.getKey(entity.item.item)
    }

    override fun clearEntities(clearConfig: ClearConfig, levels: List<ServerLevel>): Int {
        if (!enabled) return 0
        var removalCount = 0
        for (level in levels) {
            try {
                val entities = level.getEntities(EntityTypeTest.forClass(ItemEntity::class.java)) { true }.filter { entity ->
                    if (entity.hasCustomName() && !clearConfig.clearNamed) return@filter false
                    if (entity.isPassenger && !clearConfig.clearPassengers) return@filter false
                    if (entity.age == -32768 && !clearConfig.clearPersistent) return@filter false
                    return@filter shouldClear(entity)
                }
                entities.forEach { entity -> entity.remove(Entity.RemovalReason.KILLED) }
                removalCount += entities.size
            } catch (exception: Exception) {
                Utils.printError("An exception was thrown while attempting to clear entities: + $exception")
                exception.printStackTrace()
            }
        }
        return removalCount
    }

    override fun getPlaceholder(): String {
        return "%clear_amount_items%"
    }

    override fun toString(): String {
        return "ItemClearable(enabled=$enabled, blacklist=$blacklist, whitelist=$whitelist)"
    }
}
