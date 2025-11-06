package com.example.duoforce

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class Act_Guardar : AppCompatActivity() {

    private lateinit var nuevaContra: EditText
    private lateinit var repetirContra: EditText
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_act_guardar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Vincular elementos del layout
        nuevaContra = findViewById(R.id.nueva_contra)
        repetirContra = findViewById(R.id.repetir_nueva_contra)
        btnGuardar = findViewById(R.id.btn_guardar_new_contra)

        // Acción del botón Guardar
        btnGuardar.setOnClickListener {
            val pass1 = nuevaContra.text.toString().trim()
            val pass2 = repetirContra.text.toString().trim()

            when {
                pass1.isEmpty() -> alerta("Contraseña vacía", "Debes ingresar una nueva contraseña.")
                pass2.isEmpty() -> alerta("Confirmación vacía", "Debes repetir la contraseña.")
                pass1 != pass2 -> alerta("No coinciden", "Ambas contraseñas deben ser iguales.")
                !validarPasswordFuerte(pass1) ->
                    alerta(
                        "Contraseña débil",
                        "Debe tener mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 símbolo."
                    )
                else -> {
                    cambiarPassword(pass1)
                }
            }
        }
    }

    // Mostrar alertas con SweetAlertDialog
    private fun alerta(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .show()
    }

    // Validación de contraseña fuerte
    private fun validarPasswordFuerte(pass: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*#?&]).{8,}\$")
        return regex.containsMatchIn(pass)
    }

    // Envío de la nueva contraseña al servidor PHP
    private fun cambiarPassword(nuevaPassword: String) {
        val email = intent.getStringExtra("email") ?: ""
        val url = "http://54.89.22.17/cambiarContrasenia.php"

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.contains("actualizada")) {
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("¡Contraseña actualizada!")
                        .setContentText("Ahora puedes iniciar sesión.")
                        .setConfirmText("Ir al Login")
                        .setConfirmClickListener { dialog ->
                            dialog.dismissWithAnimation()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .show()
                } else {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("No se pudo cambiar la contraseña.")
                        .show()
                }
            },
            {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar con el servidor.")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["nueva_contra"] = nuevaPassword
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
