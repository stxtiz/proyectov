package com.example.duoforce

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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

lateinit var nombre: EditText
lateinit var apellido: EditText
lateinit var email: EditText
lateinit var clave: EditText
lateinit var clave2: EditText
lateinit var btn_reg: Button
lateinit var btn_ir_login: Button
lateinit var btn_ir_recuperar: Button

class Act_Registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_act_registro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nombre = findViewById(R.id.txtnombre)
        apellido = findViewById(R.id.txtapellido)
        email = findViewById(R.id.txtemail)
        clave = findViewById(R.id.txtclave)
        clave2 = findViewById(R.id.txtclave2)
        btn_reg = findViewById(R.id.btn_registro)

        // Enlaces visibles a login y recuperación (requisitos)
        btn_ir_login = Button(this).apply {
            text = "Ir a Login"
            setOnClickListener {
                startActivity(Intent(this@Act_Registro, MainActivity::class.java))
            }
        }

        btn_ir_recuperar = Button(this).apply {
            text = "Recuperar Contraseña"
            setOnClickListener {
                startActivity(Intent(this@Act_Registro, Act_Recuperar::class.java))
            }
        }

        // Listener principal
        btn_reg.setOnClickListener {
            val nom = nombre.text.toString().trim()
            val ape = apellido.text.toString().trim()
            val mail = email.text.toString().trim()
            val pass = clave.text.toString().trim()
            val pass2 = clave2.text.toString().trim()

            // 1️⃣ Validar campos vacíos
            if (nom.isEmpty() || ape.isEmpty() || mail.isEmpty() || pass.isEmpty() || pass2.isEmpty()) {
                alerta("Campos obligatorios", "Debes completar todos los campos.")
                return@setOnClickListener
            }

            // 2️⃣ Validar formato de correo
            if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                alerta("Correo inválido", "Por favor ingresa un formato de correo válido.")
                return@setOnClickListener
            }

            // 3️⃣ Validar contraseñas iguales
            if (pass != pass2) {
                alerta("Contraseñas distintas", "Las contraseñas deben coincidir.")
                return@setOnClickListener
            }

            // 4️⃣ Validar robustez de la contraseña
            if (!validarPasswordRobusta(pass)) {
                alerta(
                    "Contraseña débil",
                    "Debe tener al menos 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial."
                )
                return@setOnClickListener
            }

            // 5️⃣ Enviar datos al servidor
            registrarUsuario(nom, ape, mail, pass)
        }
    }

    private fun alerta(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Aceptar")
            .show()
    }

    private fun validarPasswordRobusta(pass: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*#?&]).{8,}\$")
        return regex.containsMatchIn(pass)
    }

    private fun registrarUsuario(nom: String, ape: String, mai: String, cla: String) {
        val url = "http://54.89.22.17/registrarUsuario.php"

        btn_reg.isEnabled = false // Evita clics dobles

        val request = object : StringRequest(Method.POST, url,
            { response ->
                btn_reg.isEnabled = true
                when {
                    response.contains("\"estado\":1") -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Registro exitoso")
                            .setContentText("Tu cuenta ha sido creada correctamente. Ahora puedes iniciar sesión.")
                            .setConfirmText("Ir a Login")
                            .setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .show()
                    }
                    response.contains("\"estado\":2") -> {
                        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Correo existente")
                            .setContentText("El correo ingresado ya está registrado. Usa otro o recupera tu cuenta.")
                            .setConfirmText("Recuperar")
                            .setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                                startActivity(Intent(this, Act_Recuperar::class.java))
                            }
                            .show()
                    }
                    response.contains("\"estado\":0") -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error del servidor")
                            .setContentText("No se pudo completar el registro. Inténtalo más tarde.")
                            .setConfirmText("Ok")
                            .show()
                    }
                    else -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Respuesta inesperada")
                            .setContentText("El servidor devolvió un formato no reconocido.")
                            .setConfirmText("Ok")
                            .show()
                    }
                }
            },
            { _ ->
                btn_reg.isEnabled = true
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar con el servidor.")
                    .setConfirmText("Ok")
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["nombre"] = nom
                params["apellido"] = ape
                params["email"] = mai
                params["password"] = cla
                return params
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }
}
