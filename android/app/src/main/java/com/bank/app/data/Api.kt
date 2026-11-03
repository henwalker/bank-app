package com.bank.app.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val customerNumber: Int)
data class Account(val accountNumber: String, val balance: Float, val nickname: String?)
data class NicknameRequest(val nickname: String?)

interface BankApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/accounts")
    suspend fun getAccounts(): Response<List<Account>>

    @GET("api/accounts/{accountNumber}")
    suspend fun getAccount(@Path("accountNumber") accountNumber: String): Response<Account>

    @PATCH("api/accounts/{accountNumber}/nickname")
    suspend fun updateNickname(
        @Path("accountNumber") accountNumber: String,
        @Body request: NicknameRequest
    ): Response<Account>
}

object SessionManager {
    var token: String? = null
    var customerNumber: Int? = null

    fun clear() {
        token = null
        customerNumber = null
    }
}

object ApiClient {
    // 10.0.2.2 is how the Android emulator reaches localhost on the computer
    private const val BASE_URL = "http://localhost:8080/"

    private val authInterceptor = Interceptor { chain ->
        val builder = chain.request().newBuilder()
        SessionManager.token?.let { builder.addHeader("Authorization", "Bearer $it") }
        chain.proceed(builder.build())
    }

    val api: BankApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().addInterceptor(authInterceptor).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BankApi::class.java)
}


