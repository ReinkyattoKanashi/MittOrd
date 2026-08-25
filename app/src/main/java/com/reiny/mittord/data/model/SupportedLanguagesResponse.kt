package com.reiny.mittord.data.model

import com.google.gson.annotations.SerializedName

data class SupportedLanguagesResponse(
    @SerializedName("tl") val targetLanguages: Map<String, String> = emptyMap()
)