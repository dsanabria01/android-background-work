package com.example.exempleworkmanager.api

import android.util.Log
import com.example.exempleworkmanager.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/**
 * Thin wrapper around the Retrofit client for the "dites" API.
 * The base URL comes from `api.baseUrl` in local.properties (see README).
 */
object CrudApi {

    private const val TAG = "CrudApi"

    private val service: DitesApiService by lazy {
        val client = OkHttpClient.Builder()
            .apply {
                // Body logging only in debug builds.
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }
            .build()

        Retrofit.Builder()
            .client(client)
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DitesApiService::class.java)
    }

    /**
     * Returns the first "dita" from the API, or null if the request fails
     * or the server returns an empty list.
     */
    suspend fun getDita(): Dita? =
        try {
            val response = service.getDita()
            if (response.isSuccessful) response.body()?.firstOrNull() else null
        } catch (e: IOException) {
            Log.w(TAG, "Network error while fetching dita", e)
            null
        }
}
