package com.pokeskies.skiesclear.config.clearables

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

    fun shouldClear(entity: Entity): Boolean {
        if (entity !is ItemEntity) return false
        if (getBlacklistedItems().any { it == entity.item.item }) return false
        getWhitelistedItems().let { list ->
            if (list.isNotEmpty() && list.none { it == entity.item.item }) return false
        }

        return true
    }

    // Returns a list of items that should be blacklisted, but uses a cache to avoid recalculating the list every time
    private fun getBlacklistedItems(): List<Item> {
        if (blacklistedItems != null) return blacklistedItems!!
        val newEntities = mutableListOf<Item>()
        for (entry in blacklist) {
            newEntities.add(BuiltInRegistries.ITEM.get(ResourceLocation(entry)))
        }
        blacklistedItems = newEntities
        return newEntities
    }

    // Returns a list of items that should be whitelisted, but uses a cache to avoid recalculating the list every time
    private fun getWhitelistedItems(): List<Item> {
        if (whitelistedItems != null) return whitelistedItems!!
        val newEntities = mutableListOf<Item>()
        for (entry in whitelist) {
            newEntities.add(BuiltInRegistries.ITEM.get(ResourceLocation(entry)))
        }
        whitelistedItems = newEntities
        return newEntities
    }

    override fun toString(): String {
        return "ItemClearable(enabled=$enabled, blacklist=$blacklist, whitelist=$whitelist, blacklistedItems=$blacklistedItems, whitelistedItems=$whitelistedItems)"
    }
}