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
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.json.JSONObject


class LoginFragment : Fragment() {
    lateinit var sharedpreferences: SharedPreferences
    val Preference = "session"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        sharedpreferences = activity!!.getSharedPreferences(Preference, Context.MODE_PRIVATE)


        view.loginButton.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(context, "You must fill all the fields", Toast.LENGTH_SHORT).show()
            }else{
                loader.show()
                val queue = Volley.newRequestQueue(context)
                val url = "http://test.azweb.dk/api/auth/login"
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
                            Toast.makeText(context, "Email or password is incorrect", Toast.LENGTH_SHORT).show()
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

}
