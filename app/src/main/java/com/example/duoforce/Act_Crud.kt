package com.example.duoforce

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

lateinit var btning: Button
lateinit var btnlis: Button

class Act_Crud : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_act_crud)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btning=findViewById(R.id.btning)
        btnlis=findViewById(R.id.btnlis)

        btning.setOnClickListener {
            val Act_Registro = Intent(this, Act_Registro::class.java)
            startActivity(Act_Registro)

        }
        btnlis.setOnClickListener {
            val Act_Listado = Intent(this, Act_Listado::class.java)
            startActivity(Act_Listado)

        }
    }
}