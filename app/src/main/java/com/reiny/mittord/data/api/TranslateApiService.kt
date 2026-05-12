package com.reiny.mittord.data.api

import com.reiny.mittord.data.model.SupportedLanguagesResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslateApiService {

    @GET("translate_a/single")
    suspend fun detectAndTranslate(
        @Query("client") client: String,
        @Query("sl") sl: String,
        @Query("tl") tl: String,
        @Query("dt") dt: String,
        @Query("q") q: String
    ): ResponseBody

    @GET("translate_a/l")
    suspend fun getSupportedLanguages(
        @Query("client") client: String,
        @Query("tl") tl: String
    ): SupportedLanguagesResponse
}