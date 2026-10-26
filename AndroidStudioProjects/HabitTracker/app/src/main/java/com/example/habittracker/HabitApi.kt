
package com.example.habittracker

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.PUT

interface HabitApi {
    @GET("habits")
    suspend fun getHabits(): List<Habit>

    @POST("habits")
    suspend fun addHabit(@Body habit: Habit): Habit

    @DELETE("habits/{id}")
    suspend fun deleteHabit(@Path("id") id: Int)

    @PUT("habits/{id}")
    suspend fun updateHabit(
        @Path("id") id: Int,
        @Body habit: Habit
    ): Habit
}
