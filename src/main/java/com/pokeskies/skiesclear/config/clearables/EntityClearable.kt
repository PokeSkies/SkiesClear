package com.pokeskies.skiesclear.config.clearables

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType

class EntityClearable(
    val enabled: Boolean = true,
    var blacklist: List<String> = emptyList(),
    var whitelist: List<String> = emptyList()
) {
    @Transient
    private var blacklistedEntities: List<EntityType<*>>? = null
    @Transient
    private var whitelistedEntities: List<EntityType<*>>? = null

    fun shouldClear(entity: Entity): Boolean {
        if (getBlacklistedEntities().any { it == entity.type }) return false
        getWhitelistedEntities().let { list ->
            if (list.isNotEmpty() && list.none { it == entity.type }) return false
        }

        return true
    }

    // Returns a list of entities that should be blacklisted, but uses a cache to avoid recalculating the list every time
    private fun getBlacklistedEntities(): List<EntityType<*>> {
        if (blacklistedEntities != null) return blacklistedEntities!!
        val newEntities = mutableListOf<EntityType<*>>()
        for (entry in blacklist) {
            newEntities.add(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation(entry)))
        }
        blacklistedEntities = newEntities
        return newEntities
    }

    // Returns a list of entities that should be whitelisted, but uses a cache to avoid recalculating the list every time
    private fun getWhitelistedEntities(): List<EntityType<*>> {
        if (whitelistedEntities != null) return whitelistedEntities!!
        val newEntities = mutableListOf<EntityType<*>>()
        for (entry in whitelist) {
            newEntities.add(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation(entry)))
        }
        whitelistedEntities = newEntities
        return newEntities
    }

    override fun toString(): String {
        return "EntityClearable(enabled=$enabled, blacklist=$blacklist, whitelist=$whitelist, blacklistedEntities=$blacklistedEntities, whitelistedEntities=$whitelistedEntities)"
    }
}