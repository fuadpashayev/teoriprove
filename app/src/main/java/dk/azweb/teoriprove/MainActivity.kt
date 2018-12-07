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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedpreferences = getSharedPreferences(Preference,Context.MODE_PRIVATE)
        TOKEN = sharedpreferences.getString("token",null)
        supportActionBar!!.hide()
        if(TOKEN!=null)
            Home()
        openSignIn.setOnClickListener {
            navigation.hide()
            registerPage.hide()
            backButton.show()
            loginPage.show()
        }

        openRegistration.setOnClickListener {
            navigation.hide()
            loginPage.hide()
            backButton.show()
            registerPage.show()
        }

        backButton.setOnClickListener {
            navigation.show()
            backButton.hide()
            loginPage.hide()
            registerPage.hide()
        }

        loginButton.setOnClickListener {
            val email = loginName.text.toString()
            val password = loginPassword.text.toString()
            val queue = Volley.newRequestQueue(this)
            val url = "http://test.azweb.dk/api/auth/login"
            val postRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        val res = JSONObject(response)
                        val token = res.getString("access_token")
                        val editor = sharedpreferences.edit()
                        editor.putString("token",token)
                        editor.apply()
                        Home()
                    },
                    Response.ErrorListener {
                        Log.d("-------Error", "error")
                    }
            ) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["email"] = email
                    params["password"] = password

                    return params
                }
            }
            queue.add(postRequest)
        }

        registerButton.setOnClickListener {
            val email = registerEmail.text.toString()
            val name = registerName.text.toString()
            val password = registerPassword.text.toString()
            val queue = Volley.newRequestQueue(this)
            val url = "http://test.azweb.dk/api/auth/register"
            val postRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        val res = JSONObject(response)
                        val token = res.getString("access_token")
                        val editor = sharedpreferences.edit()
                        editor.putString("token",token)
                        editor.apply()
                        Home()
                    },
                    Response.ErrorListener {
                        Log.d("-------Error", "error")
                    }
            ) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["email"] = email
                    params["name"] = name
                    params["password"] = password
                    params["password_confirmation"] = password
                    return params
                }
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    return headers
                }
            }
            queue.add(postRequest)
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

    override fun onBackPressed() {

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


fun Fragment.start(){
    val transaction = manager.beginTransaction()
    val fragment = this
    var currentTag = manager!!.fragments.toString()
    currentTag = Regex(".*[\\[|,](.*)Fragment.*").replace(currentTag,"$1").trim()
    var newTag = fragment.toString()
    newTag = Regex("(.*)Fragment.*").replace(newTag,"$1")
    if(newTag != currentTag) {
        Toast.makeText(this@HomeActivity,"click olundu", Toast.LENGTH_SHORT).show()
        transaction.setCustomAnimations(R.anim.abc_fade_in, 0)
        transaction.replace(R.id.main_frame, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}



interface ServerCallback {
    fun onSuccess(result: JSONObject?)
    fun onError(error: VolleyError)
}







