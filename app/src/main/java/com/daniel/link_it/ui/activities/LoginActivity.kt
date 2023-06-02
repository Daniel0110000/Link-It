package com.daniel.link_it.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.daniel.link_it.R
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPref = getSharedPreferences("linkitPrefs", Context.MODE_PRIVATE)
        val requestQueue = Volley.newRequestQueue(this)
        fun reqPostLogin(url: String, json: JSONObject) {
            val solicitud = JsonObjectRequest(
                Request.Method.POST, url, json,
                Response.Listener { response ->
                    // Manejar la respuesta de la petición
                    val status = response.getString("status")
                    Toast.makeText(applicationContext,status,Toast.LENGTH_SHORT).show()
                    if (status.equals("200")){
                        Toast.makeText(this, "TOdo bem", Toast.LENGTH_SHORT).show()
                        val editor = sharedPref.edit()
                        editor.putString("username", json.getString("username"))
                        editor.putString("password", json.getString("password"))
                        editor.apply()
                    }else{
                        Toast.makeText(this, "Credenciales incorretas", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error ->
                    // Manejar el fallo de la petición
                    Toast.makeText(applicationContext,error.toString(), Toast.LENGTH_SHORT).show()
                    print(error.toString())
                })

            requestQueue.add(solicitud)
        }
        val hasSesion = sharedPref.getString("username", null)
        if(hasSesion != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        //Toast.makeText(this,hasSesion.toString(), Toast.LENGTH_SHORT).show()
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener{

            val username = findViewById<EditText>(R.id.username)
            val password = findViewById<EditText>(R.id.password)
            val chLogin = findViewById<CheckBox>(R.id.chLogin)
            val url = "https://link-it-production.up.railway.app/test"
            val json = JSONObject()
            json.put("username", username.text)
            json.put("password", password.text)
            json.put("chosed_login", chLogin.isChecked)
            json.put("java", true)
            Toast.makeText(applicationContext,json.toString(), Toast.LENGTH_SHORT).show()
            reqPostLogin(url, json)
        }
    }
}