package com.example.duoforce

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

// =================== VARIABLES GLOBALES ===================
lateinit var fecha: TextView
lateinit var temp: TextView
lateinit var hum: TextView
lateinit var imagenTemp: ImageView
lateinit var datos: RequestQueue

private lateinit var flashSwitch: Switch
private lateinit var ampolletaSwitch: Switch
private lateinit var cameraManager: CameraManager
private var cameraId: String? = null

val mHandler = Handler(Looper.getMainLooper())

class Act_Panel_Control : AppCompatActivity() {

    // ============ REFRESCAR DATOS CADA 1s ===============
    private val refrescar = object : Runnable {
        override fun run() {
            fecha.text = fechahora()
            obtenerDatos()
            mHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_act_panel_control)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias UI
        fecha = findViewById(R.id.txt_fecha)
        temp = findViewById(R.id.txt_temp)
        hum = findViewById(R.id.txt_humedad)
        imagenTemp = findViewById(R.id.imagen_temp)
        flashSwitch = findViewById(R.id.switch_flash)
        ampolletaSwitch = findViewById(R.id.switch_ampolleta)

        datos = Volley.newRequestQueue(this)

        // ========== CONFIGURACIÓN DEL FLASH =============
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
        }

        // Evento para linterna
        flashSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                encenderFlash()
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Linterna encendida")
                    .setContentText("La linterna del dispositivo está activada.")
                    .setConfirmText("OK")
                    .show()
            } else {
                apagarFlash()
            }
        }

        // Evento para ampolleta (simulada)
        ampolletaSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Ampolleta encendida")
                    .setContentText("La ampolleta se ha activado correctamente.")
                    .show()
            } else {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Ampolleta apagada")
                    .setContentText("La ampolleta se ha apagado.")
                    .show()
            }
        }
    }

    // ========== FUNCIÓN FECHA Y HORA ==========
    fun fechahora(): String {
        val c: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMMM YYYY, hh:mm:ss a")
        return sdf.format(c.time)
    }

    // ========== OBTENER DATOS DEL SERVIDOR ==========
    private fun obtenerDatos() {
        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    temp.text = "${response.getString("temperatura")} °C"
                    hum.text = "${response.getString("humedad")} %"
                    val valor = response.getString("temperatura").toFloat()
                    cambiarImagen(valor)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                error.printStackTrace()
            }
        )
        datos.add(request)
    }

    private fun cambiarImagen(valor: Float) {
        if (valor >= 20) {
            imagenTemp.setImageResource(R.drawable.tempalta)
        } else {
            imagenTemp.setImageResource(R.drawable.tempbaja)
        }
    }

    // ================= FLASH CONTROL ===================
    private fun encenderFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraId != null) {
            cameraManager.setTorchMode(cameraId!!, true)
        }
    }

    private fun apagarFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraId != null) {
            cameraManager.setTorchMode(cameraId!!, false)
            flashSwitch.isChecked = false
        }
    }

    override fun onResume() {
        super.onResume()
        mHandler.post(refrescar)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(refrescar)
        apagarFlash()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(refrescar)
        apagarFlash()
    }
}
