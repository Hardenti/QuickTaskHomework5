package com.example.quicktask

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.example.quicktask.AddHabitActivity

class MainActivity : AppCompatActivity() {

    private lateinit var habitsListTextView: TextView
    private lateinit var addHabitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        habitsListTextView = findViewById(R.id.habitsListTextView)
        addHabitButton = findViewById(R.id.addHabitButton)

        val newHabit = intent.getStringExtra("habit_name")
        if (!newHabit.isNullOrEmpty()) {
            val current = habitsListTextView.text.toString()
            val updated = if (current.contains("No habits yet")) {
                "- $newHabit"
            } else {
                "$current\n- $newHabit"
            }
            habitsListTextView.text = updated
        }

        addHabitButton.setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }
    }
}
