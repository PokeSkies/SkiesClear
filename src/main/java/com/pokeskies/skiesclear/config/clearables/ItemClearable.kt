package com.pokeskies.skiesclear.config.clearables

import com.pokeskies.skiesclear.utils.Utils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item

class ItemClearable(
    val enabled: Boolean = true,
    var blacklist: List<String> = emptyList(),
    var whitelist: List<String> = emptyList()
) {
    @Transient
    private var blacklistedItems: List<Item>? = null
    @Transient
    private var whitelistedItems: List<Item>? = null

    // Confirms if the passed entity is the correct type for this clearable
    fun isEntityType(entity: Entity): Boolean {
        return entity is ItemEntity
    }

    fun shouldClear(entity: Entity): Boolean {
        if (entity !is ItemEntity) return false
        if (blacklistedItems == null) blacklistedItems = createEntitiesList(blacklist)
        if (blacklistedItems!!.any { it == entity.item.item }) return false

        if (whitelistedItems == null) whitelistedItems = createEntitiesList(whitelist)
        whitelistedItems!!.let { list ->
            if (list.isNotEmpty() && list.none { it == entity.item.item }) return false
        }

        return true
    }

    private fun createEntitiesList(list: List<String>): MutableList<Item> {
        val newEntities = mutableListOf<Item>()
        for (entry in list) {
            if (Utils.wildcardPattern.matcher(entry).matches()) {
                val namespace = entry.split(":")[0]
                for (type in BuiltInRegistries.ITEM) {
                    if (BuiltInRegistries.ITEM.getKey(type).namespace == namespace) {
                        newEntities.add(type)
                    }
                }
                continue
            } else {
                newEntities.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry)))
            }
        }
        return newEntities
    }

    override fun toString(): String {
        return "ItemClearable(enabled=$enabled, blacklist=$blacklist, whitelist=$whitelist, blacklistedItems=$blacklistedItems, whitelistedItems=$whitelistedItems)"
    }
}
