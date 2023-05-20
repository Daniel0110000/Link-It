package com.daniel.link_it.ui.activities

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.daniel.link_it.R
// Socket.IO
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONObject
import org.json.JSONArray
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ScrollView

class MainActivity : AppCompatActivity() {
    object SocketHandler {
        lateinit var mSocket: Socket
        @Synchronized
        fun setSocket() {
            try {
                mSocket = IO.socket("https://link-it-production.up.railway.app")
            } catch (e: URISyntaxException) {

            }
        }
        @Synchronized
        fun getSocket(): Socket {
            return mSocket
        }
        @Synchronized
        fun establishConnection() {
            mSocket.connect()
        }
        @Synchronized
        fun closeConnection() {
            mSocket.disconnect()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("linkitPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)
        SocketHandler.setSocket()
        SocketHandler.establishConnection()
        val mSocket = SocketHandler.getSocket()
        val inputMsg = findViewById<EditText>(R.id.inputMsg)
        val btnSnd = findViewById<Button>(R.id.btnSnd)
        val btnMenu = findViewById<Button>(R.id.btnMenu)
        val overlay = findViewById<LinearLayout>(R.id.overlay)

        Toast.makeText(this,username, Toast.LENGTH_SHORT).show()
        val userData = JSONObject()
        userData.put("user", username)
        mSocket.emit("getUser",userData)

        val scrollView = findViewById<ScrollView>(R.id.scroll)
        val msgContainer = findViewById<LinearLayout>(R.id.msgCont)
        val backgroundDrawable = resources.getDrawable(R.drawable.backgroundradious)

        fun addToView(msgData: JSONObject){
            val tv = TextView(this)
            tv.setTextColor(Color.parseColor("#FFFFFF"))
            tv.background = backgroundDrawable
            tv.setPadding(35,20,35,20)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(10, 20, 10, 20)
            tv.layoutParams = layoutParams
            tv.text = msgData.getString("message")
            msgContainer.addView(tv)
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }

        val profileLbl = findViewById<TextView>(R.id.profileUser)
        profileLbl.text = username
        mSocket.on("getMessages") { args ->
            if (args[0] != null) {
                val msgsJSONArray = args[0] as JSONArray
                runOnUiThread {
                    for(i in 0 until msgsJSONArray.length()){
                        addToView(msgsJSONArray.getJSONObject(i))
                    }
                }
            }
        }
        mSocket.on("chat:message") {args ->
            if (args[0] != null){
                runOnUiThread {
                    val msgsJSONObj = args[0] as JSONObject
                    addToView(msgsJSONObj)
                }
            }
        }

        btnSnd.setOnClickListener{
            val msgData = JSONObject()
            msgData.put("message", inputMsg.text)
            msgData.put("user", username)
            msgData.put("folder", "main")
            //Toast.makeText(this,msgData.toString(), Toast.LENGTH_SHORT).show()
            mSocket.emit("chat:message", msgData)
            inputMsg.text = null
        }
        btnMenu.setOnClickListener{
            val menuLayout = findViewById<LinearLayout>(R.id.menu_layout)
            menuLayout.visibility = View.VISIBLE
            overlay.visibility = View.VISIBLE
        }
        overlay.setOnClickListener{
            val menuLayout = findViewById<LinearLayout>(R.id.menu_layout)
            menuLayout.visibility = View.INVISIBLE
            overlay.visibility = View.INVISIBLE
        }
    }
}