
package com.example.habittracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.runtime.LaunchedEffect
import java.time.LocalDate
import androidx.activity.compose.BackHandler

private val Cream = Color(0xFFFFFBFF)
private val Blush = Color(0xFFF4EEFF)
private val Rose = Color(0xFFB9A5E8)
private val DarkRose = Color(0xFF776493)
private val Brown = Color(0xFF494253)
private val MutedBrown = Color(0xFF9A93A5)
private val Sage = Color(0xFFE8DFF7)
private val DeepSage = Color(0xFF9B85C4)
private val White = Color(0xFFFFFFFF)
private val BorderPink = Color(0xFFEDE5F7)

private data class HabitItem(
    val name: String,
    val emoji: String,
    val completed: Boolean = false
)

private fun saveHabits(context: Context, habits: List<HabitItem>) {
    val array = JSONArray()

    habits.forEach { habit ->
        array.put(
            JSONObject().apply {
                put("name", habit.name)
                put("emoji", habit.emoji)
                put("completed", habit.completed)
            }
        )
    }

    context.getSharedPreferences("habit_data", Context.MODE_PRIVATE)
        .edit()
        .putString("habits", array.toString())
        .apply()
}

private fun loadHabits(context: Context): List<HabitItem> {
    val saved = context.getSharedPreferences(
        "habit_data", Context.MODE_PRIVATE
    ).getString("habits", null) ?: return emptyList()

    return try {
        val array = JSONArray(saved)
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            HabitItem(
                name = item.getString("name"),
                emoji = item.getString("emoji"),
                completed = item.optBoolean("completed", false)
            )
        }
    } catch (e: Exception) {
        return emptyList()
    }
}



private fun saveHabitHistory(
    context: Context,
    history: org.json.JSONArray
) {
    val filteredHistory = org.json.JSONArray()
    val dateFormat = java.text.SimpleDateFormat(
        "yyyy-MM-dd",
        java.util.Locale.US
    )
    val cutoffCalendar = java.util.Calendar.getInstance()
    cutoffCalendar.add(java.util.Calendar.DAY_OF_YEAR, -89)
    val cutoffDate = dateFormat.format(cutoffCalendar.time)
   
    for (i in 0 until history.length()) {
        val entry = history.getJSONObject(i)
        val date = entry.getString("date")

        if (date >= cutoffDate) {
            filteredHistory.put(entry)
        }
    }

    context.getSharedPreferences("habit_data", Context.MODE_PRIVATE)
        .edit()
        .putString("habit_history", filteredHistory.toString())
        .apply()
}


fun loadHabitHistory(context: Context): JSONArray {
    val saved = context.getSharedPreferences(
        "habit_data",
        Context.MODE_PRIVATE
    ).getString("habit_history", "[]")

    return try {
        JSONArray(saved)
    } catch (e: Exception) {
        JSONArray()
    }
}



private fun recordTodayHistory(
    context: Context,
    habits: List<HabitItem>
) {
    val prefs = context.getSharedPreferences(
        "habit_data",
        Context.MODE_PRIVATE
    )

    val today = java.text.SimpleDateFormat(
        "yyyy-MM-dd",
        java.util.Locale.US
    ).format(java.util.Date())

    val history = try {
        org.json.JSONArray(
            prefs.getString("habit_history", "[]")
        )
    } catch (e: Exception) {
        org.json.JSONArray()
    }

    val updatedHistory = org.json.JSONArray()
    var todayEntry: org.json.JSONObject? = null

    for (i in 0 until history.length()) {
        val entry = history.getJSONObject(i)

        if (entry.optString("date") == today) {
            todayEntry = entry
        } else {
            updatedHistory.put(entry)
        }
    }

    // Keep today's existing habits, including ones now deleted.
    val habitRecords = org.json.JSONArray()

    habits.forEach { habit ->
        var found = false

        for (i in 0 until habitRecords.length()) {
            val existing = habitRecords.getJSONObject(i)

            if (existing.optString("name") == habit.name) {
                existing.put("emoji", habit.emoji)
                existing.put("completed", habit.completed)
                found = true
                break
            }
        }

        if (!found) {
            habitRecords.put(
                org.json.JSONObject().apply {
                    put("name", habit.name)
                    put("emoji", habit.emoji)
                    put("completed", habit.completed)
                }
            )
        }
    }

    updatedHistory.put(
        org.json.JSONObject().apply {
            put("date", today)
            put("habits", habitRecords)
        }
    )

    saveHabitHistory(context, updatedHistory)
}






class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HabitTrackerScreen()
            }
        }
    }
}

@Composable
fun HabitTrackerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var habits by remember { mutableStateOf(loadHabits(context)) }
    LaunchedEffect(Unit) {
        val today = java.time.LocalDate.now().toString()
        val prefs = context.getSharedPreferences(
            "habit_data",
            android.content.Context.MODE_PRIVATE
        )
        val lastDate = prefs.getString("last_reset_date", "")

        if (lastDate != today) {
            habits = habits.map { it.copy(completed = false) }
            prefs.edit().putString("last_reset_date", today).apply()
        }
    }

    var showAddHabit by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    BackHandler(showHistory) {
        showHistory = false
    }
    var newHabitName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("⭐") }

    var backendHabits by remember {
        mutableStateOf<List<Habit>>(emptyList())
    }
    var backendError by remember { mutableStateOf<String?>(null) }
    var loadingBackend by remember { mutableStateOf(false) }

    val completed = habits.count { it.completed }
    val progress = if (habits.isEmpty()) 0f
    else completed.toFloat() / habits.size

    var resetComplete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val today = java.time.LocalDate.now().toString()
        val prefs = context.getSharedPreferences(
            "habit_data",
            android.content.Context.MODE_PRIVATE
        )
        val lastDate = prefs.getString("last_reset_date", "")

        if (lastDate != today) {
            habits = habits.map { it.copy(completed = false) }
            prefs.edit().putString("last_reset_date", today).apply()
        }

        resetComplete = true
    }

    LaunchedEffect(habits, resetComplete) {
        if (resetComplete) {
            saveHabits(context, habits)
            recordTodayHistory(context, habits)
        }
    }

    if (showHistory) {
        CalendarHistoryScreen(
            context = context,
            onBack = { showHistory = false }
        )
    } else {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Blush, RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Text(
                "YOUR LITTLE SPACE 🌷", color = DarkRose,
                fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "My Habits",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Brown
            )

            Spacer(Modifier.height(5.dp))

            Spacer(Modifier.height(14.dp))

            Text(
                "♡  one day at a time  ♡",
                color = DarkRose, fontSize = 14.sp
            )
        }

        // Progress summary

        // Progress summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Today's progress ✨",
                            color = Brown, fontSize = 19.sp,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text("$completed of ${habits.size} habits completed",
                            color = MutedBrown, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(Sage, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${(progress * 100).toInt()}%",
                            color = DeepSage,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(18.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp),
                    color = DeepSage,
                    trackColor = Sage,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // Habit list heading
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${habits.size} habits",
                    fontSize = 13.sp,
                    color = MutedBrown
                )

                Text(
                    "🗓️ History",
                    modifier = Modifier.clickable {
                        showHistory = true
                    },
                    fontSize = 13.sp,
                    color = DarkRose,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))
            Text("${habits.size} habits",
                fontSize = 13.sp, color = MutedBrown)
        }

        habits.forEachIndexed { index, habit ->
            HabitCard(
                name = habit.name,
                emoji = habit.emoji,
                completed = habit.completed,

                onCompletionChange = { checked ->
                    habits = habits.mapIndexed { i, item ->
                        if (i == index) item.copy(completed = checked) else item
                    }

                    scope.launch {
                        try {
                            backendHabits = RetrofitClient.api.getHabits()

                            val backendHabit = backendHabits.firstOrNull {
                                it.name.equals(habit.name, ignoreCase = true)
                            }

                            if (backendHabit != null) {
                                RetrofitClient.api.updateHabit(
                                    backendHabit.id,
                                    backendHabit.copy(completedToday = checked)
                                )

                                backendHabits = RetrofitClient.api.getHabits()
                            } else {
                                backendError = "Backend habit not found for: ${habit.name}"
                            }
                        } catch (e: Exception) {
                            backendError = "Could not update habit: ${e.message}"
                        }
                    }
                },

                onDelete = {
                    scope.launch {
                        try {
                            // Refresh backend data first
                            backendHabits = RetrofitClient.api.getHabits()

                            val habitToDelete = backendHabits.find {
                                it.name == habit.name
                            }

                            if (habitToDelete != null) {
                                RetrofitClient.api.deleteHabit(habitToDelete.id)
                                backendHabits = RetrofitClient.api.getHabits()
                            }

                            habits = habits.filterIndexed { i, _ -> i != index }
                            recordTodayHistory(context, habits)

                        } catch (e: Exception) {
                            backendError = "Could not delete habit: ${e.message}"
                        }
                    }
                }
            )
        }

        // Add habit form
        if (showAddHabit) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderPink)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Create a new habit 🌸",
                        fontSize = 19.sp, fontWeight = FontWeight.Bold,
                        color = Brown)

                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("Habit name") },
                        placeholder = { Text("e.g. Read for 10 minutes") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Rose,
                            focusedLabelColor = DarkRose,
                            cursorColor = Rose
                        )
                    )

                    Text("Choose an emoji",
                        color = MutedBrown, fontSize = 14.sp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("⭐", "💧", "🚶", "🧘", "📚",
                            "🏃", "🥗", "💤", "🎯", "❤️").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (selectedEmoji == emoji) Blush
                                        else Cream,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (selectedEmoji == emoji) "✓$emoji"
                                    else emoji,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }

                    Button(

                        onClick = {
                            val name = newHabitName.trim()

                            if (name.isNotEmpty()) {
                                scope.launch {
                                    try {
                                        val savedHabit = RetrofitClient.api.addHabit(
                                            Habit(
                                                id = 0,
                                                name = name,
                                                description = name,
                                                completedToday = false
                                            )
                                        )

                                        habits = habits + HabitItem(
                                            savedHabit.name,
                                            selectedEmoji
                                        )

                                        backendHabits = RetrofitClient.api.getHabits()
                                        newHabitName = ""
                                        selectedEmoji = "⭐"
                                        showAddHabit = false

                                    } catch (e: Exception) {
                                        backendError = "Could not save habit: ${e.message}"
                                    }
                                }
                            }
                        },

                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Rose
                        )
                    ) {
                        Text("Save my habit ♡",
                            color = White, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        "Cancel",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { showAddHabit = false }
                            .padding(6.dp),
                        color = MutedBrown
                    )
                }
            }
        } else {
            Button(
                onClick = { showAddHabit = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Rose
                )
            ) {
                Text("+  Add a new habit",
                    color = White, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold)
            }
        }

        // Optional backend loading section
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(20.dp),
//            colors = CardDefaults.cardColors(containerColor = Blush)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//                Text("Connected app data ☁️",
//                    color = Brown, fontWeight = FontWeight.Bold,
//                    fontSize = 16.sp)
//
//                Text("Load habits from your backend when it is available.",
//                    color = MutedBrown, fontSize = 13.sp)
//
//                OutlinedButton(
//                    onClick = {
//                        loadingBackend = true
//                        backendError = null
//
//                        scope.launch {
//                            try {
//                                backendHabits = RetrofitClient.api.getHabits()
//                                println("SYNC RESULT: $backendHabits")
//                            } catch (e: Exception) {
//                                backendError = e.toString()
//                                println("SYNC ERROR: ${e.message}")
//                            } finally {
//                                loadingBackend = false
//                            }
//                        }
//                    },
//                    enabled = !loadingBackend,
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(14.dp)
//                ) {
//                    Text(if (loadingBackend) "Connecting..."
//                    else "Sync backend habits")
//                }
//
//                backendError?.let {
//                    Text("Connection issue: $it",
//                        color = DarkRose, fontSize = 12.sp)
//                }
//
//                backendHabits.forEach { habit ->
//                    Text("${habit.name} — ${habit.description}",
//                        color = Brown, fontSize = 14.sp)
//                }
//            }
//        }

        Text(
            "You're doing better than you think ♡",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MutedBrown,
            fontSize = 13.sp
        )
    }
    }
}

@Composable
fun CalendarHistoryScreen(
    context: Context,
    onBack: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }

    val history = loadHabitHistory(context)
    val historyByDate = remember(history.toString()) {
        buildMap {
            for (i in 0 until history.length()) {
                val entry = history.getJSONObject(i)
                put(entry.optString("date"), entry)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blush)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            "← Back to my habits",
            modifier = Modifier.clickable { onBack() },
            color = DarkRose,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "My Habit Calendar 🗓️",
            color = Brown,
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "‹",
                modifier = Modifier
                    .clickable { currentMonth = currentMonth.minusMonths(1) }
                    .padding(8.dp),
                fontSize = 28.sp,
                color = DarkRose
            )

            Text(
                currentMonth.month.name.lowercase()
                    .replaceFirstChar { it.uppercase() } +
                        " ${currentMonth.year}",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Brown
            )

            Text(
                "›",
                modifier = Modifier
                    .clickable { currentMonth = currentMonth.plusMonths(1) }
                    .padding(8.dp),
                fontSize = 28.sp,
                color = DarkRose
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(Modifier.fillMaxWidth()) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Text(
                            it,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = MutedBrown,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val firstDayOffset =
                    currentMonth.dayOfWeek.value - 1
                val daysInMonth =
                    currentMonth.lengthOfMonth()
                val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7

                for (weekStart in 0 until totalCells step 7) {
                    Row(Modifier.fillMaxWidth()) {
                        for (column in 0..6) {
                            val cell = weekStart + column
                            val dayNumber = cell - firstDayOffset + 1

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber in 1..daysInMonth) {
                                    val date = currentMonth
                                        .withDayOfMonth(dayNumber)
                                        .toString()
                                    val hasHistory = historyByDate.containsKey(date)
                                    val isSelected = selectedDate == date

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                if (isSelected) Blush else Color.Transparent,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedDate = date },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            "$dayNumber",
                                            color = if (isSelected) DarkRose else Brown,
                                            fontWeight = if (isSelected)
                                                FontWeight.Bold else FontWeight.Normal
                                        )

                                        Text(
                                            if (hasHistory) "•" else " ",
                                            color = DeepSage,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            "Habits on $selectedDate",
            color = Brown,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        val selectedEntry = historyByDate[selectedDate]

        if (selectedEntry == null) {
            Text(
                "No saved habit history for this date 🌱",
                color = MutedBrown,
                fontSize = 14.sp
            )
        } else {
            val dayHabits = selectedEntry.getJSONArray("habits")

            if (dayHabits.length() == 0) {
                Text("No habits recorded for this day.", color = MutedBrown)
            }

            for (i in 0 until dayHabits.length()) {
                val habit = dayHabits.getJSONObject(i)
                val completed = habit.optBoolean("completed", false)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            habit.optString("emoji", "⭐"),
                            fontSize = 22.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            habit.optString("name"),
                            modifier = Modifier.weight(1f),
                            color = Brown
                        )
                        Text(
                            if (completed) "✓ Done" else "○ Not done",
                            color = if (completed) DeepSage else MutedBrown,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitCard(
    name: String,
    emoji: String,
    completed: Boolean,
    onCompletionChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completed) Color(0xFFF0F5EC) else White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, if (completed) Sage else BorderPink
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (completed) Sage else Blush,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (completed) DeepSage else Brown
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    if (completed) "Lovely work! ✨" else "One small step at a time",
                    fontSize = 11.sp,
                    color = MutedBrown
                )
            }

            Checkbox(
                checked = completed,
                onCheckedChange = onCompletionChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = DeepSage,
                    uncheckedColor = Rose,
                    checkmarkColor = White
                )
            )

            Text(
                "×",
                modifier = Modifier
                    .clickable { onDelete() }
                    .padding(start = 5.dp, end = 2.dp),
                fontSize = 23.sp,
                color = MutedBrown
            )
        }
    }
}
