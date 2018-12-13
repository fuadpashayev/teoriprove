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
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import org.json.JSONObject
import org.json.JSONException
import org.json.JSONArray
import android.R.attr.data
import com.android.volley.VolleyError
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets





class RegisterFragment : Fragment() {
    lateinit var sharedpreferences: SharedPreferences
    val Preference = "session"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        sharedpreferences = activity!!.getSharedPreferences(Preference, Context.MODE_PRIVATE)


        view.registerButton.setOnClickListener {
            val email = email.text.toString()
            val name = name.text.toString()
            val password = password.text.toString()
            val password_confirm = password_confirm.text.toString()
            var error:String? = null
            if(name.isEmpty()) {
                error = "Name required"
            }else if(email.isEmpty()){
                error = "Email required"
            }else if(!email.isEmail()){
                error = "Email format is wrong. ex: user@user.com"
            }else if(password.isEmpty()){
                error = "Password required"
            }else if(password_confirm.isEmpty()){
                error = "Confirmation password required"
            }else if(password_confirm != password){
                error = "Passwords does not fit"
            }else if(password.length<6){
                error = "Password must be minimum 6 character"
            }


            if(error!=null){
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }else {

                loader.show()
                val queue = Volley.newRequestQueue(context)
                val url = "http://test.azweb.dk/api/auth/register"
                val postRequest = object : StringRequest(Request.Method.POST, url,
                        Response.Listener { response ->
                            val res = JSONObject(response)
                            val token = res.getString("access_token")
                            val editor = sharedpreferences.edit()
                            editor.putString("token", token)
                            editor.apply()
                            Home()
                            loader.hide()
                        },
                        Response.ErrorListener {
                            loader.hide()
                            Toast.makeText(context, "Error Happened", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    override fun getParams(): Map<String, String> {
                        val params = HashMap<String, String>()
                        params["email"] = email
                        params["name"] = name
                        params["password"] = password
                        params["password_confirmation"] = password_confirm
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

        view.backButton.setOnClickListener {
            Back()
        }

        return view
    }

    fun Back(){
        activity!!.onBackPressed()
    }

    fun Home(){
        val intent = Intent(context,HomeActivity::class.java)
        startActivity(intent)
    }

    fun View.show(){
        this.visibility = View.VISIBLE
    }
    fun View.hide(){
        this.visibility = View.GONE
    }

    fun String.isEmail(): Boolean {
        val ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
        val p = java.util.regex.Pattern.compile(ePattern)
        val m = p.matcher(this)
        return m.matches()
    }

}
