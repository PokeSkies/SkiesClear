package com.pokeskies.skiesclear.config.clearables

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.pokeskies.skiesclear.config.ClearConfig
import com.pokeskies.skiesclear.utils.Utils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest

class CobblemonClearable(
    enabled: Boolean = true,
    blacklist: List<String> = emptyList(),
    whitelist: List<String> = emptyList()
): Clearable<PokemonEntity>(enabled, blacklist, whitelist) {
    @Transient
    private lateinit var blacklistedAspects: List<String>
    @Transient
    private lateinit var whitelistedAspects: List<String>
    @Transient
    private lateinit var blacklistedTags: List<String>
    @Transient
    private lateinit var whitelistedTags: List<String>

    override fun initialize() {
        blacklistedAspects = generateAspects(blacklist)
        whitelistedAspects = generateAspects(whitelist)
        blacklistedTags = generateTags(blacklist)
        whitelistedTags = generateTags(whitelist)
    }

    override fun getResourceLocation(entity: PokemonEntity): ResourceLocation {
        return entity.pokemon.species.resourceIdentifier
    }

    override fun clearEntities(clearConfig: ClearConfig, levels: List<ServerLevel>): Int {
        if (!enabled) return 0
        var removalCount = 0
        for (level in levels) {
            try {
                val entities = level.getEntities(EntityTypeTest.forClass(PokemonEntity::class.java)) { true }.filter { entity ->
                    if (entity.isPersistenceRequired && !clearConfig.clearPersistent) return@filter false
                    if (entity.hasCustomName() && !clearConfig.clearNamed) return@filter false
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

    override fun isBlacklisted(entity: PokemonEntity, id: ResourceLocation): Boolean {
        val pokemon = entity.pokemon
        if (pokemon.isPlayerOwned() && blacklist.contains("#owned")) return true
        if (entity.isBattling && blacklist.contains("#battling")) return true
        if (pokemon.shiny && blacklist.contains("#shiny")) return true
        if (pokemon.isLegendary() && blacklist.contains("#legendary")) return true
        if (pokemon.isMythical() && blacklist.contains("#mythical")) return true
        if (pokemon.isUltraBeast() && blacklist.contains("#ultrabeast")) return true
        if (entity.isBusy && blacklist.contains("#busy")) return true
        if (entity.isUncatchable() && blacklist.contains("#uncatchable")) return true

        // Species matching
        if (blacklist.stream().anyMatch { species: String ->
                id.toString().equals(species, ignoreCase = true)
            }) return true

        // Aspect matching
        if (blacklistedAspects.isNotEmpty()) {
            if (pokemon.aspects.isNotEmpty()) {
                if (pokemon.aspects.stream().anyMatch { pokemonAspect: String ->
                        blacklistedAspects.contains(
                            pokemonAspect
                        )
                    }) return true
            }
        }

        // Tag matching
        if (blacklistedTags.isNotEmpty()) {
            if (entity.tags.isNotEmpty()) {
                if (entity.tags.stream().anyMatch { pokemonTag: String ->
                        blacklistedTags.contains(pokemonTag)
                    }) return true
            }
        }

        return false
    }

    override fun isWhitelisted(entity: PokemonEntity, id: ResourceLocation): Boolean {
        val pokemon = entity.pokemon
        if (pokemon.isPlayerOwned() && whitelist.contains("#owned")) return true
        if (entity.isBattling && whitelist.contains("#battling")) return true
        if (pokemon.shiny && whitelist.contains("#shiny")) return true
        if (pokemon.isLegendary() && whitelist.contains("#legendary")) return true
        if (pokemon.isMythical() && whitelist.contains("#mythical")) return true
        if (pokemon.isUltraBeast() && whitelist.contains("#ultrabeast")) return true
        if (entity.isBusy && blacklist.contains("#busy")) return true
        if (entity.isUncatchable() && blacklist.contains("#uncatchable")) return true

        // Species matching
        if (whitelist.stream().anyMatch { species: String ->
                pokemon.species.resourceIdentifier.toString().equals(species, ignoreCase = true)
            }) return true

        // Aspect matching
        if (whitelistedAspects.isNotEmpty()) {
            if (pokemon.aspects.isNotEmpty()) {
                if (pokemon.aspects.stream().anyMatch { pokemonAspect: String ->
                        whitelistedAspects.contains(
                            pokemonAspect
                        )
                    }) return true
            }
        }

        // Tag matching
        if (whitelistedTags.isNotEmpty()) {
            if (entity.tags.isNotEmpty()) {
                if (entity.tags.stream().anyMatch { pokemonTag: String ->
                        whitelistedTags.contains(pokemonTag)
                    }) return true
            }
        }

        return false
    }

    override fun getPlaceholder(): String {
        return "%clear_amount_pokemon%"
    }

    private fun generateAspects(list: List<String>): List<String> {
        val newAspects = mutableListOf<String>()
        for (entry in list) {
            if (entry.startsWith("#aspect=", true)) {
                // split the entry string at the FIRST = occurrence
                val split = entry.split("=", ignoreCase = true, limit = 2)
                if (split.size == 2) newAspects.add(split[1])
            }
        }
        return newAspects
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

    override fun toString(): String {
        return "CobblemonClearable(enabled=$enabled, blacklist=$blacklist, whitelist=$whitelist, " +
                "blacklistedAspects=$blacklistedAspects, whitelistedAspects=$whitelistedAspects)"
    }
}
