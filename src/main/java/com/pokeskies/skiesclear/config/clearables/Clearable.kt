package com.pokeskies.skiesclear.config.clearables

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.pokeskies.skiesclear.config.ClearConfig
import com.pokeskies.skiesclear.utils.Utils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import java.lang.reflect.Type

abstract class Clearable<T : Entity>(
    val enabled: Boolean = false,
    var blacklist: List<String> = emptyList(),
    var whitelist: List<String> = emptyList()
) {
    abstract fun getResourceLocation(entity: T): ResourceLocation

    open fun initialize() {}

    // Given a list over levels, clear the relevant entities and return a count of how many were cleared
    abstract fun clearEntities(clearConfig: ClearConfig, levels: List<ServerLevel>): Int

    open fun shouldClear(entity: T): Boolean {
        val rl = getResourceLocation(entity)

        if (isBlacklisted(entity, rl)) return false
        if (whitelist.isNotEmpty() && !isWhitelisted(entity, rl)) return false

        return true
    }

    open fun isBlacklisted(entity: T, id: ResourceLocation): Boolean {
        return blacklist.any {
            if (Utils.wildcardPattern.matcher(it).matches()) {
                val namespace = it.split(":")[0]
                id.namespace.equals(namespace, ignoreCase = true)
            } else {
                id.asString().equals(it, ignoreCase = true)
            }
        }
    }

    open fun isWhitelisted(entity: T, id: ResourceLocation): Boolean {
        return whitelist.any {
            if (Utils.wildcardPattern.matcher(it).matches()) {
                val namespace = it.split(":")[0]
                id.namespace.equals(namespace, ignoreCase = true)
            } else {
                id.asString().equals(it, ignoreCase = true)
            }
        }
    }

    inline fun <reified T> getType(): Class<T> {
        return T::class.java
    }

    abstract fun getPlaceholder(): String

    fun parse(string: String, total: Int): String {
        return string.replace(getPlaceholder(), total.toString())
    }

    class MapDeserializer : JsonDeserializer<Map<String, Clearable<*>>> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Map<String, Clearable<*>> {
            return json.asJsonObject.entrySet().mapNotNull { (key, value) ->
                val clearable = when (key.lowercase()) {
                    "items" -> context.deserialize<ItemClearable>(value, ItemClearable::class.java)
                    "entities" -> context.deserialize<EntityClearable>(value, EntityClearable::class.java)
                    "cobblemon" -> if (FabricLoader.getInstance().isModLoaded("cobblemon"))
                        context.deserialize<CobblemonClearable>(value, CobblemonClearable::class.java)
                    else {
                        Utils.printError("Cobblemon is not loaded, skipping Cobblemon clearable!")
                        return@mapNotNull null
                    }
                    else -> throw JsonParseException("Unknown clearable type: $key")
                }
                key to clearable
            }.toMap()
        }
    }
}
