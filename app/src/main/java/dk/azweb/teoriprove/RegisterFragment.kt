package dk.azweb.teoriprove

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_register.*
import org.json.JSONObject


class RegisterFragment : Fragment() {
    lateinit var sharedpreferences: SharedPreferences
    val Preference = "session"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
        sharedpreferences = activity!!.getSharedPreferences(Preference, Context.MODE_PRIVATE)


        registerButton.setOnClickListener {
            val email = email.text.toString()
            val name = name.text.toString()
            val password = password.text.toString()
            val password_confirm = password_confirm.text.toString()
            val queue = Volley.newRequestQueue(context)
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
        val intent = Intent(context,HomeActivity::class.java)
        startActivity(intent)
    }

}
