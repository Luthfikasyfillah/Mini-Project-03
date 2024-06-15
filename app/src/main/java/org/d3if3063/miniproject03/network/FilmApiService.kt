package org.d3if3063.miniproject03.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.d3if3063.miniproject03.model.Film
import org.d3if3063.miniproject03.model.OpStatus
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

private const val BASE_URL = "https://unspoken.my.id/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface FilmApiService {
    @GET("api_luthfi.php")
    suspend fun getFilm(
        @Header("Authorization") userId: String
    ):List<Film>
    @Multipart
    @POST("api_luthfi.php")
    suspend fun postFilm(
        @Header("Authorization") userId: String,
        @Part("title") title: RequestBody,
        @Part("genre") genre: RequestBody,
        @Part("year") year: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus
    @DELETE("api_luthfi.php")
    suspend fun deleteFilm(
        @Header("Authorization") userId: String,
        @Query("id") filmId: String
    ) : OpStatus
}


object FilmApi {
    val service: FilmApiService by lazy {
        retrofit.create(FilmApiService::class.java)
    }

    fun getFilmUrl(imageId: String): String {
        return "${BASE_URL}image.php?id=$imageId"
    }
}

enum class ApiStatus {LOADING, SUCCESS, FAILED}