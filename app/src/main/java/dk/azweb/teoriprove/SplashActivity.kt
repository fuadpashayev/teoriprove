package dk.azweb.teoriprove

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.util.Log
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONObject


class SplashActivity : AppCompatActivity() {
    lateinit var sharedpreferences: SharedPreferences
    val Preference = "session"
    var TOKEN:String?=null
    var nextActivity:Class<*> = MainActivity::class.java
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedpreferences = getSharedPreferences(Preference, Context.MODE_PRIVATE)
        TOKEN = sharedpreferences.getString("token",null)
        if(TOKEN!=null){

            Profile(this).getProfile(TOKEN,object:ServerCallback{
                override fun onSuccess(result: JSONObject?) {
                    val intent = Intent(this@SplashActivity,MainActivity::class.java)
                    intent.putExtra("loggedIn",true)
                    startActivity(intent)
                }
                override fun onError(error: VolleyError) {
                    val editor = sharedpreferences.edit()
                    editor.remove("token")
                    editor.apply()
                    val intent = Intent(this@SplashActivity,MainActivity::class.java)
                    intent.putExtra("loggedIn",false)
                    startActivity(intent)
                }
            })


        }else{
            val intent = Intent(this@SplashActivity,MainActivity::class.java)
            intent.putExtra("loggedIn",false)
            startActivity(intent)
        }






    }
}
