package com.pokeskies.skiesclear.config.clearables

import com.pokeskies.skiesclear.utils.Utils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity

class EntityClearable(
    val enabled: Boolean = true,
    var blacklist: List<String> = emptyList(),
    var whitelist: List<String> = emptyList()
) {
    @Transient
    private var blacklistedEntities: MutableList<EntityType<*>>? = null
    @Transient
    private var whitelistedEntities: MutableList<EntityType<*>>? = null

    // Confirms if the passed entity is the correct type for this clearable
    fun isEntityType(entity: Entity): Boolean {
        return true
    }

    fun shouldClear(entity: Entity): Boolean {
        if (blacklistedEntities == null) blacklistedEntities = createEntitiesList(blacklist)
        if (blacklistedEntities!!.any { it == entity.type }) return false

        if (whitelistedEntities == null) whitelistedEntities = createEntitiesList(whitelist)
        whitelistedEntities!!.let { list ->
            if (list.isNotEmpty() && list.none { it == entity.type }) return false
        }

        return true
    }

    private fun createEntitiesList(list: List<String>): MutableList<EntityType<*>> {
        val newEntities = mutableListOf<EntityType<*>>()
        for (entry in list) {
            if (Utils.wildcardPattern.matcher(entry).matches()) {
                val namespace = entry.split(":")[0]
                for (type in BuiltInRegistries.ENTITY_TYPE) {
                    if (BuiltInRegistries.ENTITY_TYPE.getKey(type).namespace == namespace) {
                        newEntities.add(type)
                    }
                }
                continue
            } else {
                newEntities.add(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entry)))
            }
        }
        return newEntities
    }

    override fun toString(): String {
        return "EntityClearable(enabled=$enabled, blacklist=$blacklist, whitelist=$whitelist, blacklistedEntities=$blacklistedEntities, whitelistedEntities=$whitelistedEntities)"
    }
}
