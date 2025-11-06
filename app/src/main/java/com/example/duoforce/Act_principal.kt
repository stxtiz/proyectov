package com.example.duoforce

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Act_principal : AppCompatActivity() {
    private lateinit var btncrudusuario: Button
    private lateinit var btndatosensor: Button
    private lateinit var btndesarrollador: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_act_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btncrudusuario=findViewById(R.id.btn_crudusuario)
        btndatosensor=findViewById(R.id.btn_datossensores)
        btndesarrollador=findViewById(R.id.btn_desarrolladores)

        btncrudusuario.setOnClickListener {
            val intent = Intent(this, Act_Crud::class.java)
            startActivity(intent)
        }

        btndatosensor.setOnClickListener {
            val intent = Intent(this, Act_Panel_Control::class.java)
            startActivity(intent)
        }
        btndesarrollador.setOnClickListener {
            val intent = Intent(this, Desarrolladores::class.java)
            startActivity(intent)
        }
    }
}