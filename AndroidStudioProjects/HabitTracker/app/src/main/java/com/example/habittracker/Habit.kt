package com.example.habittracker

data class Habit(
    val id: Int,
    val name: String,
    val description: String,
    val completedToday: Boolean
)