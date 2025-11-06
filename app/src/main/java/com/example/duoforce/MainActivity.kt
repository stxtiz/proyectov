package com.example.duoforce

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class MainActivity : AppCompatActivity() {

    private lateinit var usu: EditText
    private lateinit var clave: EditText
    private lateinit var btningresar: Button
    private lateinit var btnregistrarme: Button
    private lateinit var btnolvidocontra: Button

    private lateinit var datos: RequestQueue

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btningresar = findViewById(R.id.btn_ingresar)
        usu = findViewById(R.id.usu)
        clave = findViewById(R.id.pass)
        btnregistrarme = findViewById(R.id.btn_registrarme)
        btnolvidocontra = findViewById(R.id.btn_olvidocontra)

        datos = Volley.newRequestQueue(this)

        // Ingresar
        btningresar.setOnClickListener {
            val u = usu.text.toString().trim()
            val p = clave.text.toString().trim()

            // Campos obligatorios
            if (u.isEmpty() && p.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Campos obligatorios")
                    .setContentText("Usuario/Email y Contraseña son requeridos.")
                    .setConfirmText("Corregir")
                    .show()
                usu.error = "Requerido"
                clave.error = "Requerido"
                return@setOnClickListener
            }
            if (u.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Usuario vacío")
                    .setContentText("Debes ingresar tu Usuario o Email.")
                    .setConfirmText("Entendido")
                    .show()
                usu.error = "Requerido"
                return@setOnClickListener
            }
            if (p.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Contraseña vacía")
                    .setContentText("Debes ingresar tu contraseña.")
                    .setConfirmText("Entendido")
                    .show()
                clave.error = "Requerido"
                return@setOnClickListener
            }

            // Email inválido si parece correo
            if (u.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(u).matches()) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Email inválido")
                    .setContentText("Revisa el formato del correo electrónico.")
                    .setConfirmText("Corregir")
                    .show()
                usu.error = "Email inválido"
                return@setOnClickListener
            }

            // Loading
            btningresar.isEnabled = false
            SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
                progressHelper.barColor = resources.getColor(android.R.color.holo_blue_light, theme)
                titleText = "Verificando..."
                setCancelable(false)
                show()
                consultarDatos(u, p, this)
            }
        }

        // Registrarse (enlace visible siempre)
        btnregistrarme.setOnClickListener {
            startActivity(Intent(this, Act_Registro::class.java))
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Registro")
                .setContentText("Registro exitoso. Ya puedes iniciar sesión.")
                .setConfirmText("Ok")
                .show()
        }

        // Recuperar contraseña (enlace visible siempre)
        btnolvidocontra.setOnClickListener {
            startActivity(Intent(this, Act_Recuperar::class.java))
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Recuperación")
                .setContentText("Enlace de recuperación enviado a tu correo.")
                .setConfirmText("Listo")
                .show()
        }
    }

    private fun consultarDatos(usu: String, pass: String, loadingDialog: SweetAlertDialog) {
        val url = "http://54.89.22.17/apiconsultausu.php?usu=$usu&pass=$pass"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Mapea estados a requisitos:
                    // "0" => credenciales inválidas
                    // "3" => usuario bloqueado (según tu ejemplo)
                    // "1" => login OK
                    val estado = response.optString("estado", "")
                    when (estado) {
                        "0" -> {
                            loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                            loadingDialog.titleText = "Credenciales inválidas"
                            loadingDialog.contentText = "Usuario o contraseña incorrectos."
                            loadingDialog.confirmText = "Intentar de nuevo"
                            loadingDialog.setConfirmClickListener { it.dismissWithAnimation() }
                        }
                        "3" -> {
                            loadingDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE)
                            loadingDialog.titleText = "Usuario bloqueado"
                            loadingDialog.contentText = "Tu cuenta ha sido desactivada. Contacta al administrador."
                            loadingDialog.confirmText = "Recuperar"
                            loadingDialog.setConfirmClickListener {
                                it.dismissWithAnimation()
                                startActivity(Intent(this, Act_Recuperar::class.java))
                            }
                        }
                        "1" -> {
                            loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                            loadingDialog.titleText = "¡Bienvenido!"
                            loadingDialog.contentText = "Inicio de sesión exitoso."
                            loadingDialog.confirmText = "Continuar"
                            loadingDialog.setConfirmClickListener {
                                it.dismissWithAnimation()
                                startActivity(Intent(this@MainActivity, Act_principal::class.java))
                                finish()
                            }
                        }
                        else -> {
                            loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                            loadingDialog.titleText = "Respuesta desconocida"
                            loadingDialog.contentText = "No se pudo interpretar el estado del servidor."
                            loadingDialog.confirmText = "Ok"
                        }
                    }
                } catch (e: JSONException) {
                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                    loadingDialog.titleText = "Respuesta inválida"
                    loadingDialog.contentText = "El servidor devolvió un JSON no esperado."
                    loadingDialog.confirmText = "Ok"
                } finally {
                    btningresar.isEnabled = true
                }
            },
            { error ->
                btningresar.isEnabled = true
                loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                loadingDialog.titleText = "Error de conexión"
                loadingDialog.contentText = "No se pudo conectar con el servidor."
                loadingDialog.confirmText = "Aceptar"
            }
        ).apply {
            retryPolicy = DefaultRetryPolicy(7500, 1, 1.0f)
        }

        datos.add(request)
    }
}
