package com.example.exempleworkmanager.api

import retrofit2.Response
import retrofit2.http.GET

interface DitesApiService {
    @GET("/dites/")
    suspend fun getDita(): Response<List<Dita>>
}