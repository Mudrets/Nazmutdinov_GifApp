package com.example.gifapp.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Существующие разделы gif-изображений
 */
enum class PageSection(val value: String) {
    @SerializedName("random")
    RANDOM("random"),
    @SerializedName("top")
    TOP("top"),
    @SerializedName("latest")
    LATEST("latest"),
    @SerializedName("hot")
    HOT("hot");

    override fun toString(): String {
        return super.toString().toLowerCase(Locale.ROOT)
    }
}