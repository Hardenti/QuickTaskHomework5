package com.example.quicktask

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import com.example.quicktask.MainActivity

class AddHabitActivity : AppCompatActivity() {

    private lateinit var habitNameEditText: EditText
    private lateinit var saveHabitButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        habitNameEditText = findViewById(R.id.habitNameEditText)
        saveHabitButton = findViewById(R.id.saveHabitButton)
        backButton = findViewById(R.id.backButton)

        saveHabitButton.setOnClickListener {
            val habitName = habitNameEditText.text.toString()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("habit_name", habitName)
            startActivity(intent)
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}
