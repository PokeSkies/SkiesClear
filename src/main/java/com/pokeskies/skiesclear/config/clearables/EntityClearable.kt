package com.pokeskies.skiesclear.config.clearables

import com.pokeskies.skiesclear.SkiesClear
import com.pokeskies.skiesclear.config.ClearConfig
import com.pokeskies.skiesclear.utils.Utils
import net.minecraft.advancements.critereon.NbtPredicate
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
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
    private lateinit var blacklistedNbt: List<CompoundTag>
    @Transient
    private lateinit var whitelistedTags: List<String>
    @Transient
    private lateinit var whitelistedNbt: List<CompoundTag>

    override fun initialize() {
        blacklistedTags = generateTags(blacklist)
        whitelistedTags = generateTags(whitelist)
        blacklistedNbt = generateNBT(blacklist)
        whitelistedNbt = generateNBT(whitelist)
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

        if (blacklistedNbt.isNotEmpty()) {
            val nbt = NbtPredicate.getEntityTagToCompare(entity)
            var passes = false
            for (entry in blacklistedNbt) {
                if (passes) break
                entry.allKeys.forEach { key ->
                    if (!nbt.contains(key)) {
                        return@forEach
                    }
                    val tag = entry.get(key)
                    val entityTag = nbt.get(key)

                    if (tag != entityTag) {
                        return@forEach
                    }

                    passes = true
                }
            }

            if (passes) return true
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

        if (whitelistedNbt.isNotEmpty()) {
            val nbt = NbtPredicate.getEntityTagToCompare(entity)
            var passes = false
            for (entry in whitelistedNbt) {
                if (passes) break
                entry.allKeys.forEach { key ->
                    if (!nbt.contains(key)) {
                        return@forEach
                    }
                    val tag = entry.get(key)
                    val entityTag = nbt.get(key)

                    if (tag != entityTag) {
                        return@forEach
                    }

                    passes = true
                }
            }

            if (passes) return true
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

    private fun generateNBT(list: List<String>): List<CompoundTag> {
        val newTags = mutableListOf<CompoundTag>()
        for (entry in list) {
            if (entry.startsWith("#nbt=", true)) {
                // split the entry string at the FIRST = occurrence
                val split = entry.split("=", ignoreCase = true, limit = 2)
                if (split.size == 2) {
                    newTags.add(SkiesClear.INSTANCE.gson.fromJson(split[1], CompoundTag::class.java))
                }
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
