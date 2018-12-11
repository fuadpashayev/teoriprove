package dk.azweb.teoriprove

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.app.Fragment
import android.widget.Toast
import com.android.volley.VolleyError


class MainActivity : AppCompatActivity() {
    lateinit var sharedpreferences:SharedPreferences
    val Preference = "session"
    var TOKEN:String?=null
    val manager = supportFragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedpreferences = getSharedPreferences(Preference,Context.MODE_PRIVATE)
        TOKEN = sharedpreferences.getString("token",null)
        supportActionBar!!.hide()
        if(TOKEN!=null)
            Home()
        signIn.setOnClickListener {
            LoginFragment().start()
        }
        signUp.setOnClickListener {
            RegisterFragment().start()
        }


    }

    fun Home(){
        val intent = Intent(this,HomeActivity::class.java)
        startActivity(intent)
    }

    fun View.show(){
        this.visibility = View.VISIBLE
    }
    fun View.hide(){
        this.visibility = View.GONE
    }



    fun Fragment.start(){
        val transaction = manager.beginTransaction()
        val fragment = this
        var currentTag = manager!!.fragments.toString()
        currentTag = Regex(".*[\\[|,](.*)Fragment.*").replace(currentTag,"$1").trim()
        var newTag = fragment.toString()
        newTag = Regex("(.*)Fragment.*").replace(newTag,"$1")
        if(newTag != currentTag) {
            transaction.setCustomAnimations(R.anim.abc_fade_in, 0)
            transaction.replace(R.id.content, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }

}

class Profile(val context:Context){
    fun getProfile(token:String?,serverCallback: ServerCallback){
        val queue = Volley.newRequestQueue(context)
        val url = "http://test.azweb.dk/api/auth/me"
        var message: JSONObject?
        queue.add(object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    message = JSONObject(response)
                    serverCallback.onSuccess(message)
                },
                Response.ErrorListener {
                    serverCallback.onError(it)
                }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["Accept"] = "application/json"
                return headers
            }

        })
    }
}



interface ServerCallback {
    fun onSuccess(result: JSONObject?)
    fun onError(error: VolleyError)
}







