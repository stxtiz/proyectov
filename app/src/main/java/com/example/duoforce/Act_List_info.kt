package com.example.duoforce

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.regex.Pattern

class Act_List_info : AppCompatActivity() {

    private lateinit var nom: EditText
    private lateinit var ape: EditText
    private lateinit var email: EditText
    private lateinit var mod: Button
    private lateinit var elim: Button

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_list_info)

        // Vincular vistas
        nom = findViewById(R.id.txtnombremod)
        ape = findViewById(R.id.txtapellidomod)
        email = findViewById(R.id.txtcorreomod)
        mod = findViewById(R.id.btn_modificar)
        elim = findViewById(R.id.btn_eliminar)

        // Recibir datos del intent
        userId = intent.getStringExtra("id")
        nom.setText(intent.getStringExtra("Nombre"))
        ape.setText(intent.getStringExtra("Apellido"))
        email.setText(intent.getStringExtra("Email"))

        // Botón modificar
        mod.setOnClickListener {
            if (validarCampos()) {
                confirmarModificacion()
            }
        }

        // Botón eliminar
        elim.setOnClickListener {
            confirmarEliminacion()
        }
    }

    // ✅ Validar campos
    private fun validarCampos(): Boolean {
        val nombre = nom.text.toString().trim()
        val apellido = ape.text.toString().trim()
        val correo = email.text.toString().trim()

        if (nombre.isEmpty()) {
            alertaCampo("Campo vacío", "El campo Nombre no puede estar vacío.")
            return false
        }
        if (apellido.isEmpty()) {
            alertaCampo("Campo vacío", "El campo Apellido no puede estar vacío.")
            return false
        }
        if (correo.isEmpty()) {
            alertaCampo("Campo vacío", "El campo Email no puede estar vacío.")
            return false
        }

        if (!Pattern.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", nombre)) {
            alertaCampo("Nombre inválido", "Solo se permiten letras y espacios.")
            return false
        }
        if (!Pattern.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", apellido)) {
            alertaCampo("Apellido inválido", "Solo se permiten letras y espacios.")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            alertaCampo("Correo inválido", "El formato del correo no es correcto.")
            return false
        }

        return true
    }

    private fun alertaCampo(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .show()
    }

    // ⚠ Confirmar modificación
    private fun confirmarModificacion() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Guardar cambios?")
            .setContentText("¿Estás seguro de modificar este usuario?")
            .setConfirmText("Sí, modificar")
            .setCancelText("Cancelar")
            .setConfirmClickListener {
                it.dismissWithAnimation()
                modificarUsuario()
            }
            .show()
    }

    // ⚠ Confirmar eliminación
    private fun confirmarEliminacion() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Eliminar usuario?")
            .setContentText("Esta acción no se puede deshacer.")
            .setConfirmText("Sí, eliminar")
            .setCancelText("Cancelar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                eliminarUsuario()
            }
            .show()
    }

    // 🔄 Modificar usuario en servidor
    private fun modificarUsuario() {
        val url = "http://54.89.22.17/modificar_usuario.php"

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                when {
                    response.contains("success") -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Actualizado")
                            .setContentText("El usuario fue modificado correctamente.")
                            .setConfirmText("OK")
                            .setConfirmClickListener {
                                it.dismissWithAnimation()
                                finish()
                            }
                            .show()
                    }
                    response.contains("email_exists") -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Correo duplicado")
                            .setContentText("Ya existe otro usuario con ese correo.")
                            .show()
                    }
                    else -> {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("No se pudo actualizar el usuario.")
                            .show()
                    }
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
                params["id"] = userId ?: ""
                params["nombre"] = nom.text.toString()
                params["apellido"] = ape.text.toString()
                params["email"] = email.text.toString()
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    // ❌ Eliminar usuario en servidor
    private fun eliminarUsuario() {
        val url = "http://54.89.22.17/eliminar_usuario.php"

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.contains("success")) {
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Eliminado")
                        .setContentText("El usuario fue eliminado correctamente.")
                        .setConfirmText("OK")
                        .setConfirmClickListener { dialog ->
                            dialog.dismissWithAnimation()
                            finish()
                        }
                        .show()
                } else {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("No se pudo eliminar el usuario.")
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
                params["id"] = userId ?: ""
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
