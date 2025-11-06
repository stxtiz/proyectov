package com.example.duoforce

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import cn.pedant.SweetAlert.SweetAlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

lateinit var nombre: EditText
lateinit var apellido: EditText
lateinit var email: EditText
lateinit var clave: EditText
lateinit var btn_reg: Button

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
        btn_reg = findViewById(R.id.btn_registro)

        btn_reg.setOnClickListener {
            val nom = nombre.text.toString().trim()
            val ape = apellido.text.toString().trim()
            val mail = email.text.toString().trim()
            val pass = clave.text.toString().trim()

            when {
                nom.isEmpty() -> alertaCampo("Nombre vacío", "Debes ingresar tu nombre.")
                ape.isEmpty() -> alertaCampo("Apellido vacío", "Debes ingresar tu apellido.")
                mail.isEmpty() -> alertaCampo("Correo vacío", "Debes ingresar un correo electrónico.")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches() ->
                    alertaCampo("Correo inválido", "El formato del correo no es correcto.")
                pass.isEmpty() -> alertaCampo("Contraseña vacía", "Debes ingresar una contraseña.")
                !validarPasswordFuerte(pass) ->
                    alertaCampo(
                        "Contraseña débil",
                        "Debe tener mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 símbolo."
                    )
                else -> guardar(nom, ape, mail, pass)
            }
        }
    }

    private fun alertaCampo(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .show()
    }

    private fun validarPasswordFuerte(pass: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*#?&]).{8,}\$")
        return regex.containsMatchIn(pass)
    }

    private fun guardar(nom: String, ape: String, mai: String, cla: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase

        try {
            val datos = ContentValues().apply {
                put("Nombre", nom)
                put("Apellido", ape)
                put("Email", mai)
                put("Clave", cla)
            }

            val resultado = db.insert("USUARIOS", null, datos)
            if (resultado != -1L) {
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Registro exitoso")
                    .setContentText("Los datos se guardaron correctamente.")
                    .setConfirmText("Ver listado")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        val listado = Intent(this, Act_Listado::class.java)
                        startActivity(listado)
                        finish()
                    }
                    .show()
            } else {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("No se pudo insertar el registro.")
                    .show()
            }

        } catch (e: Exception) {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("Ocurrió un problema: ${e.message}")
                .show()
        } finally {
            db.close()
        }
    }
}
