package dk.azweb.teoriprove

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.android.volley.VolleyError
import java.util.*


class MainActivity : AppCompatActivity() {
    val manager = supportFragmentManager
    var connectedMessage = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        ActionBar.hide()
        if(!checkPermission())
            requestPermission()
        val loggedIn = intent.extras["loggedIn"] as Boolean


        checkInternetConnection(networkStatus)

        signIn.setOnClickListener {
            LoginFragment().start()
        }
        signUp.setOnClickListener {
            RegisterFragment().start()
        }


    }

    fun checkInternetConnection(networkStatusText:TextView){
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                netWorkMessage(isInternetActive(),networkStatusText)
            }
        }, 0, 3000)
    }

    fun netWorkMessage(status:Boolean, networkStatusText: TextView){
        if(status){
            runOnUiThread {
                if(!connectedMessage) {
                    networkStatusText.text = "Connected"
                    networkStatusText.setBackgroundColor(resources.getColor(R.color.green))
                    networkStatusText.show()
                    connectedMessage = true
                    Handler().postDelayed({
                        networkStatus.hide()
                    }, 1500)
                }else{
                    networkStatusText.hide()
                }
            }

        }else{
            runOnUiThread {
                networkStatusText.text = "No Internet Connection"
                networkStatusText.setBackgroundColor(resources.getColor(R.color.red))
                networkStatusText.show()
                connectedMessage=false
            }
        }
    }
    fun isInternetActive():Boolean{
        val connection = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(connection.getNetworkInfo(0).state == android.net.NetworkInfo.State.CONNECTED ||
                connection.getNetworkInfo(0).state == android.net.NetworkInfo.State.CONNECTING ||
                connection.getNetworkInfo(1).state == android.net.NetworkInfo.State.CONNECTING ||
                connection.getNetworkInfo(1).state == android.net.NetworkInfo.State.CONNECTED) {
            return true
        }else if(connection.getNetworkInfo(0).state == android.net.NetworkInfo.State.DISCONNECTED ||
                connection.getNetworkInfo(1).state == android.net.NetworkInfo.State.DISCONNECTED){
            return false
        }
        return false
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
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            transaction.replace(R.id.content, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }

    fun checkPermission():Boolean{
        val phoneState = ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE)
        return phoneState == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(){
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_PHONE_STATE),1)
    }

}

class Profile(val context:Context){
    fun getProfile(token:String?, errorCallback: ((VolleyError) -> Unit)? = null, successCallback: (JSONObject)->Unit){
        val queue = Volley.newRequestQueue(context)
        val url = "http://test.azweb.dk/api/auth/me"
        var message: JSONObject?
        queue.add(object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    message = JSONObject(response)
                    successCallback(message!!)
                },
                Response.ErrorListener {
                    errorCallback?.invoke(it)
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










