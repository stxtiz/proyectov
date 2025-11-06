package com.example.duoforce

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

private lateinit var listado: ListView
private lateinit var listausuario: ArrayList<String>
private lateinit var listaJSON: JSONArray

class Act_Listado : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_act_listado)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        listado = findViewById(R.id.lista)
        cargarUsuariosActivos()

        // 👇 Evento de clic en la lista
        listado.setOnItemClickListener { _, _, position, _ ->
            try {
                val user = listaJSON.getJSONObject(position)
                val id = user.getInt("id").toString()
                val nombre = user.getString("nombres")
                val apellido = user.getString("apellidos")
                val email = user.getString("email")

                // Intent hacia Act_List_info
                val intent = Intent(this, Act_List_info::class.java).apply {
                    putExtra("id", id)
                    putExtra("Nombre", nombre)
                    putExtra("Apellido", apellido)
                    putExtra("Email", email)
                }
                startActivity(intent)
            } catch (e: Exception) {
                mostrarAlerta("Error", "No se pudieron obtener los datos del usuario seleccionado.")
            }
        }
    }

    private fun cargarUsuariosActivos() {
        val url = "http://54.89.22.17/listarUsuarios.php"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    val estado = json.optInt("estado", 0)

                    if (estado == 1) {
                        listaJSON = json.getJSONArray("usuarios")
                        listausuario = ArrayList()

                        for (i in 0 until listaJSON.length()) {
                            val user = listaJSON.getJSONObject(i)
                            val id = user.getInt("id")
                            val nombre = user.getString("nombres")
                            val apellido = user.getString("apellidos")
                            val email = user.getString("email")
                            listausuario.add("$id - $nombre - $apellido - $email")
                        }

                        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listausuario)
                        listado.adapter = adapter
                    } else {
                        mostrarAlerta("Sin usuarios", "No se encontraron usuarios activos.")
                    }
                } catch (e: Exception) {
                    mostrarAlerta("Error", "El servidor devolvió un formato inválido.")
                }
            },
            { mostrarAlerta("Error de conexión", "No se pudo conectar con el servidor.") }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun mostrarAlerta(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Aceptar")
            .show()
    }

    override fun onStart() {
        super.onStart()
        cargarUsuariosActivos()
    }
}
