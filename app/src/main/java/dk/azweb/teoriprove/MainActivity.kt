package dk.azweb.teoriprove

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


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        openSignIn.setOnClickListener {
            navigation.hide()
            backButton.show()
            loginPage.show()
        }

        backButton.setOnClickListener {
            navigation.show()
            backButton.hide()
            loginPage.hide()
            registerPage.hide()
        }

        loginButton.setOnClickListener {
            Log.d("---------a", "requested.......")
            val login = loginName.text.toString()
            val password = loginPassword.text.toString()
            val queue = Volley.newRequestQueue(this)
            val url = "http://test.azweb.dk/api/auth/login"
            val postRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        val res = JSONObject(response)
                        val token = res.getString("access_token")
                        Log.d("-------Response", response)
                        Log.d("-------token", token)
                    },
                    Response.ErrorListener {
                        Log.d("-------Error", "error")
                    }
            ) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["email"] = login
                    params["password"] = password

                    return params
                }
            }
            queue.add(postRequest)
        }

    }

    fun View.show(){
        this.visibility = View.VISIBLE
    }
    fun View.hide(){
        this.visibility = View.GONE
    }


}
