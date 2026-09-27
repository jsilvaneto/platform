package com.platform.app.data.remote

import com.platform.app.data.remote.dto.ItemDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PlatformApiService {
    @GET("items")
    suspend fun getItems(): List<ItemDto>

    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: String): ItemDto

    @POST("items")
    suspend fun createItem(@Body item: ItemDto): ItemDto

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String)

    companion object {
        const val BASE_URL = "https://api.platform.local/v1/"
    }
}
