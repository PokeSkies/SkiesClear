package com.pokeskies.skiesclear.config.clearables

import com.pokeskies.skiesclear.config.ClearConfig
import com.pokeskies.skiesclear.utils.Utils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.entity.EntityTypeTest

class EntityClearable(
    enabled: Boolean = false,
    blacklist: List<String> = emptyList(),
    whitelist: List<String> = emptyList()
): Clearable<Entity>(enabled, blacklist, whitelist) {
    @Transient
    private lateinit var blacklistedTags: List<String>
    @Transient
    private lateinit var whitelistedTags: List<String>

    override fun initialize() {
        blacklistedTags = generateTags(blacklist)
        whitelistedTags = generateTags(whitelist)
    }

    override fun getResourceLocation(entity: Entity): ResourceLocation {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
    }

    override fun clearEntities(clearConfig: ClearConfig, levels: List<ServerLevel>): Int {
        if (!enabled) return 0
        var removalCount = 0
        for (level in levels) {
            try {
                val entities = level.getEntities(EntityTypeTest.forClass(Entity::class.java)) { true }.filter { entity ->
                    if (entity is Player) return@filter false
                    if (entity is Mob && entity.isPersistenceRequired && !clearConfig.clearPersistent) return@filter false
                    if (entity.hasCustomName() && !clearConfig.clearNamed) return@filter false
                    if (entity.isPassenger && !clearConfig.clearPassengers) return@filter false
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

    override fun isBlacklisted(entity: Entity, id: ResourceLocation): Boolean {
        // Tag matching
        if (blacklistedTags.isNotEmpty()) {
            if (entity.tags.isNotEmpty()) {
                if (entity.tags.stream().anyMatch { tag: String ->
                        blacklistedTags.contains(tag)
                    }) return true
            }
        }

        return super.isBlacklisted(entity, id)
    }

    override fun isWhitelisted(entity: Entity, id: ResourceLocation): Boolean {
        // Tag matching
        if (whitelistedTags.isNotEmpty()) {
            if (entity.tags.isNotEmpty()) {
                if (entity.tags.stream().anyMatch { pokemonTag: String ->
                        whitelistedTags.contains(pokemonTag)
                    }) return true
            }
        }

        return super.isWhitelisted(entity, id)
    }

    private fun generateTags(list: List<String>): List<String> {
        val newTags = mutableListOf<String>()
        for (entry in list) {
            if (entry.startsWith("#tag=", true)) {
                // split the entry string at the FIRST = occurrence
                val split = entry.split("=", ignoreCase = true, limit = 2)
                if (split.size == 2) newTags.add(split[1])
            }
        }
        return newTags
    }

    override fun getPlaceholder(): String {
        return "%clear_amount_entities%"
    }

    override fun toString(): String {
        return "EntityClearable(enabled=$enabled, blacklist=$blacklist, whitelist=$whitelist)"
    }
}
