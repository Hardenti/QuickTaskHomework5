package com.example.quicktask

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// ⭐ DataStore instance
private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class MainActivity : AppCompatActivity() {

    // ⭐ DataStore key
    private val USER_NAME_KEY = stringPreferencesKey("user_name")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ⭐ UI elements
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val saveNameButton = findViewById<Button>(R.id.saveNameButton)
        val savedNameTextView = findViewById<TextView>(R.id.savedNameTextView)
        val habitsListTextView = findViewById<TextView>(R.id.habitsListTextView)
        val addHabitButton = findViewById<Button>(R.id.addHabitButton)

        // ⭐ Load saved habits from file on startup
        val savedHabits = loadHabitsFromFile()
        if (savedHabits.isNotEmpty()) {
            habitsListTextView.text = savedHabits
        }

        // ⭐ Save name when button is clicked
        saveNameButton.setOnClickListener {
            val name = nameInput.text.toString()
            lifecycleScope.launch {
                saveUserName(name)
            }
        }

        // ⭐ Load name automatically and update UI
        lifecycleScope.launch {
            loadUserName().collect { name ->
                savedNameTextView.text = "Hello, $name"
            }
        }

        // ⭐ Handle new habits coming from AddHabitActivity
        val newHabit = intent.getStringExtra("habit_name")
        if (!newHabit.isNullOrEmpty()) {
            val current = habitsListTextView.text.toString()
            val updated = if (current.contains("No habits yet")) {
                "- $newHabit"
            } else {
                "$current\n- $newHabit"
            }

            habitsListTextView.text = updated
            saveHabitsToFile(updated)   // ⭐ Save updated list to file
        }

        // ⭐ Navigate to AddHabitActivity
        addHabitButton.setOnClickListener {
            startActivity(android.content.Intent(this, AddHabitActivity::class.java))
        }
    }

    // ⭐ DataStore save function
    private suspend fun saveUserName(name: String) {
        dataStore.edit { prefs ->
            prefs[USER_NAME_KEY] = name
        }
    }

    // ⭐ DataStore load function
    private fun loadUserName(): Flow<String> {
        return dataStore.data.map { prefs ->
            prefs[USER_NAME_KEY] ?: "Guest"
        }
    }

    // ⭐ File Storage — Save habits to file
    private fun saveHabitsToFile(text: String) {
        openFileOutput("habits.txt", Context.MODE_PRIVATE).use {
            it.write(text.toByteArray())
        }
    }

    // ⭐ File Storage — Load habits from file
    private fun loadHabitsFromFile(): String {
        return try {
            openFileInput("habits.txt").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }
}
