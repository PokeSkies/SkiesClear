package com.pokeskies.skiesclear.config.clearables

import com.pokeskies.skiesclear.SkiesClear
import com.pokeskies.skiesclear.utils.CobblemonAdaptor
import net.minecraft.world.entity.Entity

class CobblemonClearable(
    val enabled: Boolean = true,
    var blacklist: List<String> = emptyList(),
    var whitelist: List<String> = emptyList()
) {
    @Transient
    private var blacklistedAspects: List<String>? = null
    @Transient
    private var whitelistedAspects: List<String>? = null

    fun shouldClear(entity: Entity): Boolean {
        return if (SkiesClear.COBBLEMON_PRESENT) CobblemonAdaptor.shouldClearEntity(this, entity) else false
    }

    // Returns a list of aspects that should be blacklisted, but uses a cache to avoid recalculating the list every time
    fun getBlacklistedAspects(): List<String> {
        if (blacklistedAspects != null) return blacklistedAspects!!
        val newAspects = mutableListOf<String>()
        for (entry in blacklist) {
            if (entry.startsWith("#aspect=", true)) {
                // split the entry string at the FIRST = occurrence
                val split = entry.split("=", ignoreCase = true, limit = 2)
                if (split.size == 2) newAspects.add(split[1])
            }
        }
        blacklistedAspects = newAspects
        return newAspects
    }

    fun getWhitelistedAspects(): List<String> {
        if (whitelistedAspects != null) return whitelistedAspects!!
        val newAspects = mutableListOf<String>()
        for (entry in whitelist) {
            if (entry.startsWith("#aspect=", true)) {
                // split the entry string at the FIRST = occurrence
                val split = entry.split("=", ignoreCase = true, limit = 2)
                if (split.size == 2) newAspects.add(split[1])
            }
        }
        whitelistedAspects = newAspects
        return newAspects
    }

    override fun toString(): String {
        return "CobblemonClearable(enabled=$enabled, blacklist=$blacklist, whitelist=$whitelist, blacklistedAspects=$blacklistedAspects, whitelistedAspects=$whitelistedAspects)"
    }
}