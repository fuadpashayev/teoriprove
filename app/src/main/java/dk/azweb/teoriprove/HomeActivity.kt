package dk.azweb.teoriprove

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONObject
import java.util.*

class HomeActivity : AppCompatActivity() {
    lateinit var sharedpreferences: SharedPreferences
    val Preference = "session"
    var user:User?=null
    var category:CategoryModel?=null
    val manager = supportFragmentManager
    var openedFragment:String? = null
    val fragmentTag = "CATEGORY"
    var connectedMessage = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        openedFragment = null
        supportActionBar!!.hide()
        sharedpreferences = getSharedPreferences(Preference, Context.MODE_PRIVATE)
        val TOKEN = sharedpreferences.getString("token",null)
        checkInternetConnection(networkStatus)
        Profile(this).getProfile(TOKEN,object:ServerCallback{
            override fun onSuccess(result: JSONObject?) {
                user = User(result)
                //name.text = user?.name
            }
            override fun onError(error: VolleyError) {
                logout()
            }
        })

        statistics.setOnClickListener {
            val args = Bundle()
            args.putString("user_id",user!!.id)
            StatisticsFragment().start(args)
        }

        categories.setOnClickListener {
            CategoryFragment().start()
        }

        startTest.setOnClickListener {
            val queue = Volley.newRequestQueue(this)
            val url = "http://test.azweb.dk/api/question/random/with_sub_questions"
            val postRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        val args = Bundle()
                        args.putString("data",response)
                        ExamFragment().start(args)

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


    fun Fragment.start(args:Bundle?=null){
        val transaction = manager.beginTransaction()
        val fragment = this
        if(args!=null) {
            args.putBoolean("fromCategory",false)
            fragment.arguments = args
        }
        var currentTag = manager!!.fragments.toString()
        currentTag = Regex(".*[\\[|,](.*)Fragment.*").replace(currentTag,"$1").trim()
        var newTag = fragment.toString()
        newTag = Regex("(.*)Fragment.*").replace(newTag,"$1")
        if(newTag != currentTag) {
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            transaction.replace(R.id.content, fragment,fragmentTag)
            transaction.addToBackStack(null)
            transaction.commit()
            openedFragment = newTag

        }

    }




    override fun onBackPressed() {
        when(openedFragment){
            null,"Exam" -> return
            "Statistics","Category","CategoryExaming","Examing"-> {
                if(openedFragment=="CategoryExaming")
                    super.onBackPressed()
                super.onBackPressed()
                openedFragment = null
            }
            "StatisticsView"-> {
                super.onBackPressed()
                openedFragment = "Statistics"
            }
        }
    }

    fun View.show(){
        this.visibility = View.VISIBLE
    }
    fun View.hide(){
        this.visibility = View.GONE
    }


    fun checkInternetConnection(networkStatusText: TextView){
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
}
