package dk.azweb.teoriprove

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {
    lateinit var sharedpreferences: SharedPreferences
    val Preference = "session"
    var user:User?=null
    var category:Category?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedpreferences = getSharedPreferences(Preference, Context.MODE_PRIVATE)
        val TOKEN = sharedpreferences.getString("token",null)

        Profile(this).getProfile(TOKEN,object:ServerCallback{
            override fun onSuccess(result: JSONObject?) {
                user = User(result)
                name.text = user?.name
            }
            override fun onError(error: VolleyError) {
                logout()
            }
        })

        logout.setOnClickListener {
            logout()
        }


        getCategory.setOnClickListener {



            val queue = Volley.newRequestQueue(this)
            val url = "http://test.azweb.dk/api/category"
            val postRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        category = Category(response)
                        for(cat in category!!.categories!!.iterator()){
                            Log.d("------id",cat.get("name").toString())
                        }

                    },
                    Response.ErrorListener {
                        Log.d("-------Error", "error")
                    }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    return headers
                }
            }
            queue.add(postRequest)

        }
    }

    fun logout(){
        val editor = sharedpreferences.edit()
        editor.remove("token")
        editor.apply()
        finish()
    }


    override fun onBackPressed() {

    }
}
