package com.pokeskies.skiesclear.config

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesclear.config.clearables.Clearable
import com.pokeskies.skiesclear.utils.FlexibleListAdaptorFactory

class ClearConfig(
    val enabled: Boolean = true,
    val interval: Int = 300,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val dimensions: List<String> = emptyList(),
    val messages: Messages = Messages(),
    val sounds: Sounds = Sounds(),
    @JsonAdapter(Clearable.MapDeserializer::class)
    val clearables: Map<String, Clearable<*>> = emptyMap(),
    @SerializedName("clear_persistent")
    val clearPersistent: Boolean = false,
    @SerializedName("clear_named")
    val clearNamed: Boolean = false,
    @SerializedName("inform_dimensions_only")
    val informDimensionsOnly: Boolean = true
) {
    class Messages(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val clear: List<String> = emptyList(),
        val warnings: Map<String, List<String>> = emptyMap(),
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val info: List<String> = emptyList(),
    ) {
        override fun toString(): String {
            return "Messages(clear=$clear, warnings=$warnings, info=$info)"
        }
    }

    class Sounds(
        val clear: SoundSettings? = null,
        val warnings: Map<String, SoundSettings> = emptyMap(),
    ) {
        class SoundSettings(
            val sound: String = "",
            val volume: Float = 1.0f,
            val pitch: Float = 1.0f,
        ) {
            override fun toString(): String {
                return "SoundSettings(sound='$sound', volume=$volume, pitch=$pitch)"
            }
        }

        override fun toString(): String {
            return "Sounds(clear=$clear, warnings=$warnings)"
        }
    }

    override fun toString(): String {
        return "ClearConfig(enabled=$enabled, interval=$interval, dimensions=$dimensions, messages=$messages, sounds=$sounds, clearables=$clearables)"
    }

}
