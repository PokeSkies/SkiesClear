package com.pokeskies.skiesclear.utils

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.pokeskies.skiesclear.config.clearables.CobblemonClearable
import net.minecraft.world.entity.Entity

object CobblemonAdaptor {
    fun shouldClearEntity(clearable: CobblemonClearable, entity: Entity): Boolean {
        if (entity !is PokemonEntity) return false
        val pokemon = entity.pokemon

        // Blacklists. If any conditions are met, the entity should not be cleared
        if (clearable.blacklist.isNotEmpty()) {
            if (pokemon.isPlayerOwned() && clearable.blacklist.contains("#owned")) return false
            if (entity.isBattling && clearable.blacklist.contains("#battling")) return false
            if (pokemon.shiny && clearable.blacklist.contains("#shiny")) return false
            if (pokemon.isLegendary() && clearable.blacklist.contains("#legendary")) return false
            if (pokemon.isUltraBeast() && clearable.blacklist.contains("#ultrabeast")) return false
            if (entity.isBusy && clearable.blacklist.contains("#busy")) return false

            // Species matching
            if (clearable.blacklist.stream().anyMatch { species: String ->
                    pokemon.species.resourceIdentifier.toString().equals(species, ignoreCase = true)
                }) return false

            // Aspect matching
            val aspects = clearable.getBlacklistedAspects()
            if (aspects.isNotEmpty()) {
                if (pokemon.aspects.isNotEmpty()) {
                    if (pokemon.aspects.stream().anyMatch { pokemonAspect: String ->
                            aspects.contains(
                                pokemonAspect
                            )
                        }) return false
                }
            }
        }

        // Whitelist. If any conditions are met, the entity should be cleared
        if (clearable.whitelist.isNotEmpty()) {
            if (pokemon.isPlayerOwned() && clearable.whitelist.contains("#owned")) return true
            if (entity.isBattling && clearable.whitelist.contains("#battling")) return true
            if (pokemon.shiny && clearable.whitelist.contains("#shiny")) return true
            if (pokemon.isLegendary() && clearable.whitelist.contains("#legendary")) return true
            if (pokemon.isUltraBeast() && clearable.whitelist.contains("#ultrabeast")) return true

            // Species matching
            if (clearable.whitelist.stream().anyMatch { species: String ->
                    pokemon.species.resourceIdentifier.toString().equals(species, ignoreCase = true)
                }) return true

            // Aspect matching
            val aspects = clearable.getWhitelistedAspects()
            if (aspects.isNotEmpty()) {
                if (pokemon.aspects.isNotEmpty()) {
                    if (pokemon.aspects.stream().anyMatch { pokemonAspect: String ->
                            aspects.contains(
                                pokemonAspect
                            )
                        }) return true
                }
            }
            return false
        }

        return true
    }
}
