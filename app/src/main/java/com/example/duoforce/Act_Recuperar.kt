package com.example.duoforce

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Act_Recuperar : AppCompatActivity() {

    // 🔹 Campos UI
    private lateinit var correo: EditText
    private lateinit var btnRecuperar: Button
    private lateinit var c1: EditText
    private lateinit var c2: EditText
    private lateinit var c3: EditText
    private lateinit var c4: EditText
    private lateinit var c5: EditText
    private lateinit var contador: EditText

    private var codigoVigente = false // Controla si el código sigue activo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_act_recuperar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔹 Vincular elementos del layout
        correo = findViewById(R.id.correo)
        btnRecuperar = findViewById(R.id.recuperar)
        c1 = findViewById(R.id.codigo_1)
        c2 = findViewById(R.id.codigo_2)
        c3 = findViewById(R.id.codigo_3)
        c4 = findViewById(R.id.codigo_4)
        c5 = findViewById(R.id.codigo_5)
        contador = findViewById(R.id.contador)

        // 🔹 Botón "Recuperar"
        btnRecuperar.setOnClickListener {
            val email = correo.text.toString().trim()

            if (email.isEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Campo vacío")
                    .setContentText("Ingresa tu correo para continuar.")
                    .show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Correo inválido")
                    .setContentText("El formato del correo no es válido.")
                    .show()
            } else {
                enviarCodigoAlPHP(email)
            }
        }

        // 🔹 Movimiento automático entre campos de código
        configurarTextWatcher(c1, c2)
        configurarTextWatcher(c2, c3)
        configurarTextWatcher(c3, c4)
        configurarTextWatcher(c4, c5)

        // 🔹 Validar código al llenar el último campo
        c5.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (c1.text.length == 1 && c2.text.length == 1 &&
                    c3.text.length == 1 && c4.text.length == 1 &&
                    c5.text.length == 1) {

                    val email = correo.text.toString().trim()
                    val codigo = c1.text.toString() + c2.text.toString() +
                            c3.text.toString() + c4.text.toString() + c5.text.toString()

                    validarCodigoPHP(email, codigo)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // 🔹 Avanzar al siguiente campo automáticamente
    private fun configurarTextWatcher(actual: EditText, siguiente: EditText) {
        actual.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (actual.text.length == 1) siguiente.requestFocus()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ✅ Enviar el código de recuperación al correo
    private fun enviarCodigoAlPHP(email: String) {
        val url = "http://54.89.22.17/enviar_codigo.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                val json = JSONObject(response)
                val estado = json.optString("estado", "")

                when (estado) {
                    "codigo_enviado" -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Código enviado")
                            .setContentText("Revisa tu correo. El código es válido por 60 segundos.")
                            .show()
                        iniciarContador()
                    }

                    "no_existe" -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Correo no registrado")
                            .setContentText("Ese correo no está en la base de datos.")
                            .show()
                    }

                    else -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error inesperado")
                            .setContentText("No se pudo enviar el código.")
                            .show()
                    }
                }
            },
            {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar con el servidor.")
                    .show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    //  Validar el código ingresado con el servidor
    private fun validarCodigoPHP(email: String, codigo: String) {
        val url = "http://54.89.22.17/validar_codigo.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                val json = JSONObject(response)
                val estado = json.optString("estado", "")

                when (estado) {
                    "valido" -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Código correcto")
                            .setContentText("Ahora puedes restablecer tu contraseña.")
                            .setConfirmClickListener {
                                it.dismissWithAnimation()
                                val intent = Intent(this, Act_Guardar::class.java)
                                intent.putExtra("email", email)
                                startActivity(intent)
                            }
                            .show()
                    }

                    "incorrecto" -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Código incorrecto")
                            .setContentText("El código ingresado no es válido.")
                            .show()
                    }

                    "expirado" -> {
                        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Código expirado")
                            .setContentText("Han pasado más de 60 segundos, solicita uno nuevo.")
                            .show()
                    }
                }
            },
            {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de conexión")
                    .setContentText("No se pudo conectar con el servidor.")
                    .show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["codigo"] = codigo
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    // ⏱ Iniciar contador de 60 segundos
    private fun iniciarContador() {
        codigoVigente = true

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished / 1000
                contador.setText("00:$segundos")
            }

            override fun onFinish() {
                codigoVigente = false
                contador.setText("00:00")
            }
        }.start()
    }
}
