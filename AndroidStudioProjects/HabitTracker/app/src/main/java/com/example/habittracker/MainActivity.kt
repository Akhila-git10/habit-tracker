package com.example.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LinearProgressIndicator


private data class HabitItem(
    val name: String,
    val emoji: String,
    val completed: Boolean = false
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HabitTrackerScreen()
        }
    }
}

@Composable
fun HabitTrackerScreen() {
    var showAddHabit by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf( "") }
    var selectedEmoji by remember { mutableStateOf("⭐") }
    var habits by remember {
        mutableStateOf(
            listOf(
                HabitItem("Drink Water", "💧"),
                HabitItem("Walk", "🚶"),
                HabitItem("Screen Free Time", "📵"),
                HabitItem("Meditation", "🧘")
            )
        )
    }
    val completedHabits = habits.count { it.completed }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF2))
                .padding(20.dp).verticalScroll
            (rememberScrollState())
    )
    {
        Text("My Habits", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Build better habits everyday", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text( " Today " , fontSize = 20.sp)
        Text(
            text = "$completedHabits of ${habits.size} habits completed ✨  ${
                if (habits.isEmpty()) 0
                else (completedHabits * 100 / habits.size)
            }%",
            fontSize = 16.sp,
            color = Color(0xFF6A5A7A)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = {
                if (habits.isEmpty()) 0f
                else completedHabits.toFloat() / habits.size
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFF8BC9A8),
            trackColor = Color(0xFFEDE7F6)
        )
        Spacer(modifier = Modifier.height(24.dp))

        habits.forEachIndexed { index, habit ->
            HabitCard(
                name = habit.name,
                emoji = habit.emoji,
                completed = habit.completed,
                onCompletionChange = { isCompleted ->
                    habits = habits.mapIndexed { itemIndex, item ->
                        if (itemIndex == index) {
                            item.copy(completed = isCompleted)
                        } else {
                            item
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        val habitEmojis = listOf("⭐", "💧", "🚶", "🧘", "📚", "🏃", "🥗", "💤", "🎯", "❤️")
        if (showAddHabit) {
            OutlinedTextField(
                value = newHabitName,
                onValueChange = { newHabitName = it },
                label = { Text("Enter habit name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ){
                habitEmojis.forEach { emoji ->
                    Text(
                        text = if(selectedEmoji==emoji) "✓$emoji" else emoji,
                        fontSize = 28.sp ,
                        modifier = Modifier.clickable{
                            selectedEmoji = emoji
                        }
                    )
                }
            }

            Button(
                onClick = {
                    val name = newHabitName.trim()
                    if (name.isNotEmpty()) {
                        habits = habits +  HabitItem(
                            name = name,
                            emoji = selectedEmoji
                        )
                        selectedEmoji = "⭐"
                        newHabitName = ""
                        showAddHabit = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Habit")
            }
        }

        Button(
            onClick = { showAddHabit = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEDE7F6)
            )
        ) {
            Text("+ Add Habit", color = Color(0xFF6A5A7A))
        }

        }
    }


    @Composable
    fun HabitCard(
        name: String,
        emoji: String ,
        completed: Boolean,
        onCompletionChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (completed) Color(0xFFB0DCC5)
                    else Color(0xFFE3F4EC),
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$emoji $name",
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )

            Checkbox(
                checked = completed,
                onCheckedChange = { isChecked ->
                    onCompletionChange(isChecked) }
            )
            if (completed) {
                Text(
                    text = "Done! ✨",
                    fontSize = 12.sp ,
                    color = Color(0xFF356B50)
                )
            }
        }
    }




