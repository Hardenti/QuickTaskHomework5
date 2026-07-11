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

// ⭐ Sensor imports
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast

// ⭐ DataStore instance
private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class MainActivity : AppCompatActivity(), SensorEventListener {

    // ⭐ DataStore key
    private val USER_NAME_KEY = stringPreferencesKey("user_name")

    // ⭐ Sensor variables
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var lightLevelTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ⭐ UI elements
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val saveNameButton = findViewById<Button>(R.id.saveNameButton)
        val savedNameTextView = findViewById<TextView>(R.id.savedNameTextView)
        val habitsListTextView = findViewById<TextView>(R.id.habitsListTextView)
        val addHabitButton = findViewById<Button>(R.id.addHabitButton)
        lightLevelTextView = findViewById<TextView>(R.id.lightLevelTextView) // ⭐ Added for sensor display

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

        // ⭐ Initialize Light Sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            lightLevelTextView.text = "Light sensor not available"
            Toast.makeText(this, "No light sensor found on this device", Toast.LENGTH_LONG).show()
        }
    }

    // ⭐ SensorEventListener methods
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightLevel = event.values[0]
            lightLevelTextView.text = "Light Level: $lightLevel lx"

            // Optional: change background color based on brightness
            val brightness = lightLevel.coerceIn(0f, 10000f)
            val colorValue = (255 - (brightness / 10000f * 255)).toInt()
            window.decorView.setBackgroundColor(android.graphics.Color.rgb(colorValue, colorValue, colorValue))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this simple app
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
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
