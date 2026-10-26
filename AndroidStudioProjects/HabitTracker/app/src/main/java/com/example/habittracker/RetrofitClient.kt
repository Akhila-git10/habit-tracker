package com.example.habittracker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://127.0.0.1:8080/"
    //private const val BASE_URL = "http://192.168.1.11:8080/"

    val api: HabitApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HabitApi::class.java)
    }
}