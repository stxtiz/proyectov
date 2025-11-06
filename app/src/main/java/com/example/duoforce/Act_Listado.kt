package com.example.duoforce

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
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
private lateinit var buscador: SearchView
private lateinit var listausuario: ArrayList<String>
private lateinit var listaFiltrada: ArrayList<String>
private lateinit var listaJSON: JSONArray
private lateinit var adapter: ArrayAdapter<String>

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
        buscador = findViewById(R.id.buscador)

        cargarUsuariosActivos()

        // 🔹 Configurar filtro del buscador
        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarUsuarios(newText ?: "")
                return true
            }
        })

        // 🔹 Evento clic en la lista
        listado.setOnItemClickListener { _, _, position, _ ->
            try {
                // Buscar coincidencia exacta con la lista JSON
                val lineaSeleccionada = listaFiltrada[position]
                val id = lineaSeleccionada.substringBefore(" - ").trim()

                // Buscar en el JSON el usuario que tenga ese id
                for (i in 0 until listaJSON.length()) {
                    val user = listaJSON.getJSONObject(i)
                    if (user.getInt("id").toString() == id) {
                        val intent = Intent(this, Act_List_info::class.java).apply {
                            putExtra("id", id)
                            putExtra("Nombre", user.getString("nombres"))
                            putExtra("Apellido", user.getString("apellidos"))
                            putExtra("Email", user.getString("email"))
                        }
                        startActivity(intent)
                        break
                    }
                }
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

                        listaFiltrada = ArrayList(listausuario)
                        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaFiltrada)
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

    private fun filtrarUsuarios(texto: String) {
        val filtro = texto.lowercase()
        listaFiltrada.clear()

        if (filtro.isEmpty()) {
            listaFiltrada.addAll(listausuario)
        } else {
            for (item in listausuario) {
                if (item.lowercase().contains(filtro)) {
                    listaFiltrada.add(item)
                }
            }
        }
        adapter.notifyDataSetChanged()
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

